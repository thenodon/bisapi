package org.bischeck.bisapi.rest;

import java.util.Set;
import java.util.TreeSet;

import org.bischeck.bisapi.domain.Link;
import org.bischeck.bisapi.redis.JedisPoolWrapper;

import redis.clients.jedis.Jedis;

public class Util {

    private Util() {

    }

    public static Set<Link> getAllMetricLinks(String key, String prefix,
            JedisPoolWrapper jedisPool2) throws ApiException {
        Jedis jedis = null;

        Set<Link> links = new TreeSet<>();

        try {
            jedis = jedisPool2.getResource();

            // Get state links
            Set<String> stateLinks = jedis.keys(LabelText.STATE_KEY + key);
            Set<String> notificationLinks = jedis
                    .keys(LabelText.NOTIFICATION_KEY + key);

            if (!stateLinks.isEmpty()) {
                if (LabelText.STATE_KEY.equals(prefix)) {
                    links.add(new Link(stateLinks.iterator().next(),
                            LabelText.REL_SELF, LabelText.STATE_TITLE));
                } else {
                    links.add(new Link(stateLinks.iterator().next(),
                            LabelText.REL_STATE, LabelText.STATE_TITLE));
                }
            }

            if (!notificationLinks.isEmpty()) {
                if (LabelText.NOTIFICATION_KEY.equals(prefix)) {
                    links.add(new Link(notificationLinks.iterator().next(),
                            LabelText.REL_SELF, LabelText.NOTIFICATION_TITLE));
                } else {
                    links.add(new Link(notificationLinks.iterator().next(),
                            LabelText.REL_NOTIFICATION,
                            LabelText.NOTIFICATION_TITLE));

                }
            }

            // Get metric links
            Set<String> metricsLinks = jedis.keys(key + "*");
            if (!metricsLinks.isEmpty()) {
                for (String link : metricsLinks) {
                    links.add(new Link(LabelText.METRIC_KEY + link,
                            LabelText.REL_METRIC, LabelText.METRIC_TITLE));
                }
            }
        } finally {
            jedisPool2.returnResource(jedis);
        }
        return links;
    }

}
