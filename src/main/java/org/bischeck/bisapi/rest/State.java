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

package org.bischeck.bisapi.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bischeck.bisapi.domain.Message;
import org.bischeck.bisapi.domain.StateMessage;
import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.ApiError.ErrorRef;
import org.bischeck.bisapi.rest.filters.FilterFactory;
import org.bischeck.bisapi.rest.filters.FilterInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class State {

	private static final Logger LOGGER = LoggerFactory.getLogger(State.class);

	private static final String KEYHEAD = LabelText.STATE_KEY;

	private JedisPoolWrapper jedisPool;

	public State(JedisPoolWrapper jedisPool) {
		this.jedisPool = jedisPool;
	}

	public Message state(String key, Optional<String> f, Optional<String> q,
			Optional<String> from, Optional<String> to) throws ApiException {

		String decodedKey = null;

		try {
			decodedKey = URLDecoder.decode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Decoding of string {} failed- Returning the orignal",
					key, e);
			decodedKey = key;
		}
		LOGGER.debug("Key : {} ", decodedKey);

		Message message = null;

		try {
			FilterInf filter = FilterFactory.getFilter(KEYHEAD + decodedKey,
					jedisPool);

			QueryParms params = new QueryParms(f, q);
			FromTo fromto = new FromTo(from, to);

			message = filter.execute(params, fromto);
			message.setLinks(Util.getAllMetricLinks(decodedKey,
					LabelText.STATE_KEY, jedisPool));

		} catch (IllegalArgumentException e) {
			LOGGER.info("Illegal from {} to {} specification", from, to, e);
			throw new ApiException("from=" + from + "to=" + to, new ApiError(
					ErrorRef.FROMTO, "from=" + from + "to=" + to), e);
		}

		return message;
	}

	public StateMessage statusLevel(String stateLevel) {
		StateMessage stateMesg = new StateMessage();
		Set<String> res = null;
		switch (stateLevel.toLowerCase()) {
		case "critical":
			res = getStatusFromCache(stateLevel);
			stateMesg.setCritical(res);
			break;
		case "warning":
			res = getStatusFromCache(stateLevel);
			stateMesg.setWarning(res);
			break;
		case "ok":
			res = getStatusFromCache(stateLevel);
			stateMesg.setOkay(res);
			break;
		case "unknown":
			res = getStatusFromCache(stateLevel);
			stateMesg.setUnknown(res);
			break;
		default:
			res = getStatusFromCache("CRITICAL");
			stateMesg.setCritical(res);
			res = getStatusFromCache("WARNING");
			stateMesg.setWarning(res);
			res = getStatusFromCache("OK");
			stateMesg.setOkay(res);
			res = getStatusFromCache("UNKNOWN");
			stateMesg.setUnknown(res);
			break;
		}

		return stateMesg;
	}

	private Set<String> getStatusFromCache(String stateLevel) {

		Set<String> jedisRes = null;
		Jedis jedis = null;

		try {
			jedis = jedisPool.getResource();
			jedisRes = jedis.smembers(stateLevel);
		} finally {
			jedisPool.returnResource(jedis);
		}
		SortedSet<String> res = new TreeSet<String>(jedisRes);
		return res;
	}

}
