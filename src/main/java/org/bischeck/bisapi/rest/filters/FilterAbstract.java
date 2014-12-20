/*
#
# Copyright (C) 2010-2014 Anders Håål, Ingenjorsbyn AB
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
 */

package org.bischeck.bisapi.rest.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bischeck.bisapi.domain.Message;
import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.ApiException;
import org.bischeck.bisapi.rest.ApiError;
import org.bischeck.bisapi.rest.ApiError.ErrorRef;
import org.bischeck.bisapi.rest.FromTo;
import org.bischeck.bisapi.rest.JEPQuery;
import org.bischeck.bisapi.rest.QueryParms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import redis.clients.jedis.Jedis;

/**
 * The class support filtering and queries of Redis data types. The actual Redis
 * access must be done in the implementation classes<br>
 * Example of query string:<br>
 * <code>
 * ?from=2&to=10&q=state=="OK"&f=state
 * </code> <br>
 * The query string can have the following parameters:
 * <ul>
 * <li>from - search from the position. Position 0 is the data with the highest
 * score for a sorted set and last value added to a linked list. Since the score
 * is a timestamp this the newest data for the sorted set key. Default is 0 and
 * defined by {@link FromTo}.</li>
 * <li>to - end the search at this position. The parameter can also take a
 * relative number using +, like +5, but encoded as %2B5 . The only exception is
 * when query parameter q is set then the to is always regarded as the number of
 * object to retrieve, the diff between to and from. Default is 0 and defined by
 * {@link FromTo}.</li>
 * <li>q - the query expression can be define to select data that only fulfill a
 * boolean expression. All fields in the json stored structure can be used
 * except data that is part of an array. Example <br>
 * <code>q=timestamp%3C1415203620044%26%26threshold%3C2E-4%26%26state!="OK"</code>
 * <br>
 * where the %26%26 are encoded for &&, the AND operation, and %3C is the
 * encoded < sign. Default is no query.</li>
 * <li>f - the f parameter define a comma separated list of fields to be
 * displayed. Default is to show all fields.</li>
 * </ul>
 * 
 */
public abstract class FilterAbstract {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilterAbstract.class);
    private static final int INC_QUERY_SIZE = 500;

    protected JedisPoolWrapper jedisPool;
    protected String key;

    public FilterAbstract(String key, JedisPoolWrapper jedisPool) {
        this.jedisPool = jedisPool;
        this.key = key;
    }

    public Boolean keyExists(String key) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            if (jedis.exists(key)) {
                return true;
            }
        } finally {
            jedisPool.returnResource(jedis);
        }
        return false;
    }

    public String keyType(String key) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.type(key);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public Message execute(QueryParms params, FromTo fromto)
            throws ApiException {

        // DO ALL STUFF
        Set<String> res = null;

        Message message = new Message(key);

        // First step - just the result set
        res = readCacheSet(fromto);
        message.setResult(res);

        // Second step - apply query
        if (params.hasQuery()) {
            res = query(res, params.getQueries().get(0), fromto);
            message.setResult(res);
        }

        // Third step - remove fields
        if (params.hasFilters()) {
            res = filter(res, params.getFilters());
            message.setResult(res);
        }

        return message;
    }

    protected abstract Set<String> readCacheSet(FromTo fromto);

    private Set<String> filter(Set<String> res, List<String> fields)
            throws ApiException {
        TreeSet<String> resultSet = new TreeSet<String>();
        if (!res.isEmpty()) {

            // Moved outside - super more efficient
            ObjectMapper mapper = new ObjectMapper();

            for (String row : res) {

                JsonNode node = null;
                try {
                    node = mapper.readTree(row);
                } catch (IOException e) {
                    LOGGER.error(
                            "Redis data should not be de-serialized at format: {}",
                            row, e);
                    throw new ApiException(row, new ApiError(
                            ErrorRef.PARSING_JSON, row));
                }

                ObjectNode jNode = mapper.createObjectNode();

                boolean setTimestamp = true;

                for (String field : fields) {

                    if (node.has(field)) {
                        if (setTimestamp) {
                            // Make sure that timestamp is always present
                            // otherwise set will not work
                            jNode.set("timestamp", node.get("timestamp"));

                            setTimestamp = false;
                        }

                        jNode.set(field, node.get(field));

                    } else {
                        throw new ApiException(field, new ApiError(
                                ErrorRef.FIELD, field));
                    }
                }
                resultSet.add(jNode.toString());
            }
        }
        return resultSet.descendingSet();
    }

    private Set<String> query(Set<String> res, String query, FromTo fromto)
            throws ApiException {

        Set<String> queryRes = res;

        FromTo fromTo = new FromTo(fromto);

        // Must be TreeSet due to using method descendingSet below
        TreeSet<String> resultSet = new TreeSet<String>();

        LOGGER.debug("Query {} Offset {}", query, fromto.offset());

        JEPQuery jepq = new JEPQuery(query);

        while (resultSet.size() < fromto.offset()) {
            if (!queryRes.isEmpty()) {

                for (String row : queryRes) {

                    try {
                        resultSet = parseQuery(jepq, resultSet, row);
                    } catch (IOException e) {
                        LOGGER.error(
                                "Redis data should not be de-serialized at query : {}",
                                row, e);
                        throw new ApiException(row, new ApiError(
                                ErrorRef.PARSING_JSON, row));
                    }

                    // use + 1 to get correct number items
                    if (resultSet.size() == fromto.offset() + 0) {
                        return resultSet.descendingSet();
                    }
                }
            } else {
                return resultSet.descendingSet();
            }

            if (resultSet.size() < fromto.offset()) {
                LOGGER.debug("pre from {} to {}", fromTo.getFrom(),
                        fromTo.getTo());

                fromTo = new FromTo(fromTo, INC_QUERY_SIZE);

                LOGGER.debug("post from {} to {}", fromTo.getFrom(),
                        fromTo.getTo());

                queryRes = readCacheSet(fromTo);
                LOGGER.debug("RES {}", queryRes.size());
            }
        }

        return resultSet.descendingSet();
    }

    private TreeSet<String> parseQuery(JEPQuery jepq,
            TreeSet<String> resultSet, String statejson) throws IOException,
            ApiException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;

        node = mapper.readTree(statejson);

        Set<String> var = jepq.getVariables();

        // Get fields and add them
        Map<String, String> parmaters = new HashMap<String, String>();
        for (String v : var) {
            if (node.has(v)) {
                parmaters.put(v, node.get(v).toString());
            } else {
                throw new ApiException(v, new ApiError(
                        ErrorRef.QUERY_STATEMENT_FIELD, v));
            }
        }

        try {
            if (jepq.execute(parmaters)) {
                resultSet.add(statejson);
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("The query failed for {}", parmaters, e);
            throw new ApiException(jepq.toString(), new ApiError(
                    ErrorRef.QUERY_STATEMENT, jepq.toString()));
        }
        return resultSet;
    }

}
