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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.bischeck.bisapi.domain.Key;
import org.bischeck.bisapi.domain.Link;
import org.bischeck.bisapi.redis.JedisPoolWrapper;

import redis.clients.jedis.Jedis;

public class Keys {

    private JedisPoolWrapper jedisPool;

    // private final RestxErrors errors;

    public Keys(JedisPoolWrapper jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Key getByKey(String prefix, String key) {
        long startTimer = System.currentTimeMillis();

        String searchKey = key;

        Key keyRes = new Key();

        Set<String> res = null;
        if (prefix.isEmpty()) {
            // TODO must fix since its not sortedset - need select
            res = searchForMetricKey(prefix, searchKey);
        } else {
            res = searchForKey(prefix, searchKey);

        }
        keyRes.setResult(res);
        keyRes.setProcessingTime(System.currentTimeMillis() - startTimer);

        return keyRes;
    }

    private Set<String> searchForKey(String prefix, String searchKey) {
        Set<String> res;
        Set<String> keys;
        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();

            // Get all the state keys
            keys = jedis.keys(prefix + searchKey);
            if (keys.isEmpty()) {
                // throw errors
                // .on(Rules.KeyNoExistsRef.class)
                // .set(Rules.KeyNoExistsRef.KEY,
                // searchKey.substring(searchKey.indexOf('/') + 1))
                // .raise();
            }

            res = selectKeys(keys, jedis);

        } finally {
            jedisPool.returnResource(jedis);
        }

        return res;
    }

    private Set<String> searchForMetricKey(String prefix, String searchKey) {
        Set<String> res;
        Set<String> keys;
        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();

            // Get all the state keys
            keys = jedis.keys(prefix + searchKey);
            if (keys.isEmpty()) {
                // throw errors
                // .on(Rules.KeyNoExistsRef.class)
                // .set(Rules.KeyNoExistsRef.KEY,
                // searchKey.substring(searchKey.indexOf('/') + 1))
                // .raise();
            }
            Set<String> filterKeys = filteredKeys(keys, jedis);

            res = selectMetricKeys(filterKeys, jedis);

        } finally {
            jedisPool.returnResource(jedis);
        }

        return res;
    }

    /**
     * Remove all state/ notification/ and config/ prefixed keys by currently
     * only checking for linked lists.
     * 
     * @param keys
     * @return
     */
    private Set<String> filteredKeys(Set<String> keys, Jedis jedis) {
        Set<String> listKeys = new HashSet<>();
        for (String key : keys) {
            if ("list".equals(jedis.type(key))) {
                listKeys.add(key);
            }
        }
        return listKeys;
    }

    private Set<String> selectKeys(Set<String> keys, Jedis jedis) {
        Set<String> res = new TreeSet<String>();

        for (String key : keys) {

            Long stateSize = jedis.zcard(key);
            StringBuilder strbui = new StringBuilder();
            strbui.append("{ ").append(LabelText.KEY).append(key).append("\",")
                    .append(LabelText.SIZE).append(stateSize).append(',')
                    .append(LabelText.LINKS);

            strbui.append(createLinkArray(jedis, key));

            strbui.append(']').append("}");
            res.add(strbui.toString());
        }
        return res;
    }

    private StringBuilder createLinkArray(Jedis jedis, String key) {
        StringBuilder strbui = new StringBuilder();

        String rawKey = key.substring(key.indexOf('/') + 1);
        String keyPerfix = key.substring(0, key.indexOf('/') + 1);

        // LOGGER.debug("key {} raw key {} prefix {}", key, rawKey, keyPerfix);

        if (jedis.exists(LabelText.STATE_KEY + rawKey)) {
            if (LabelText.STATE_KEY.equals(keyPerfix)) {
                strbui.append(
                        new Link(LabelText.STATE_KEY + rawKey,
                                LabelText.REL_SELF, LabelText.STATE_TITLE))
                        .append(',');
            } else {
                strbui.append(
                        new Link(LabelText.STATE_KEY + rawKey,
                                LabelText.REL_RELATED, LabelText.STATE_TITLE))
                        .append(',');
            }

        }

        if (jedis.exists(LabelText.NOTIFICATION_KEY + rawKey)) {
            if (LabelText.NOTIFICATION_KEY.equals(keyPerfix)) {
                strbui.append(
                        new Link(LabelText.NOTIFICATION_KEY + rawKey,
                                LabelText.REL_SELF,
                                LabelText.NOTIFICATION_TITLE)).append(',');
            } else {
                strbui.append(
                        new Link(LabelText.NOTIFICATION_KEY + rawKey,
                                LabelText.REL_RELATED,
                                LabelText.NOTIFICATION_TITLE)).append(',');
            }

        }

        // Search for metrics
        Set<String> metricKeys = jedis.keys(key.substring(key.indexOf('/') + 1)
                + "*");
        if (!metricKeys.isEmpty()) {
            boolean first = true;
            for (String metric : metricKeys) {
                if (first) {
                    first = false;
                    strbui.append(new Link(LabelText.METRIC_KEY + metric,
                            LabelText.REL_RELATED, LabelText.METRIC_TITLE));
                } else {
                    strbui.append(',').append(
                            new Link(LabelText.METRIC_KEY + metric,
                                    LabelText.REL_RELATED,
                                    LabelText.METRIC_TITLE));
                }
            }
        }
        return strbui;
    }

    private Set<String> selectMetricKeys(Set<String> keys, Jedis jedis) {
        Set<String> res = new TreeSet<String>();

        for (String key : keys) {
            // LOGGER.debug("MetricKey input key {}", key);
            Long stateSize = jedis.llen(key);

            String serviceKey = getServiceKeyFromMetricKey(key);
            // LOGGER.debug("MetricKey service key {}", serviceKey);

            StringBuilder strbui = new StringBuilder();
            strbui.append("{ ").append(LabelText.KEY).append(key).append("\",")
                    .append(LabelText.SIZE).append(stateSize).append(',')
                    .append(LabelText.LINKS);

            strbui.append(createMetricLinkArray(jedis, key, serviceKey));

            strbui.append(']').append("}");
            res.add(strbui.toString());
        }
        return res;
    }

    private StringBuilder createMetricLinkArray(Jedis jedis, String key,
            String serviceKey) {
        StringBuilder strbui = new StringBuilder();
        strbui.append(
                new Link(LabelText.STATE_KEY + serviceKey,
                        LabelText.REL_RELATED, LabelText.STATE_TITLE))
                .append(',')
                .append(new Link(LabelText.NOTIFICATION_KEY + serviceKey,
                        LabelText.REL_RELATED, LabelText.NOTIFICATION_TITLE));

        // From the servicedef part of the key find all related metric keys
        Set<String> metricKeys = jedis.keys(serviceKey + "*");

        if (!metricKeys.isEmpty()) {
            for (String metric : metricKeys) {
                // LOGGER.debug("MetricKey key {} metricKey {}", key, metric);
                if (key.equals(metric)) {
                    // self
                    strbui.append(',')
                            .append(new Link(LabelText.METRIC_KEY + metric,
                                    LabelText.REL_SELF, LabelText.METRIC_TITLE));
                } else {
                    strbui.append(',').append(
                            new Link(LabelText.METRIC_KEY + metric,
                                    LabelText.REL_RELATED,
                                    LabelText.METRIC_TITLE));
                }
            }
        }

        return strbui;
    }

    private String getServiceKeyFromMetricKey(String key) {
        String serviceKey = key.substring(0, key.lastIndexOf('-'));
        return serviceKey;
    }

}
