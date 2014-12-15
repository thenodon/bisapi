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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.bischeck.bisapi.domain.Link;
import org.bischeck.bisapi.domain.Message;
import org.bischeck.bisapi.domain.PostStatus;
import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.ApiError.ErrorRef;
import org.bischeck.bisapi.rest.filters.FilterFactory;
import org.bischeck.bisapi.rest.filters.FilterInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.codahale.metrics.Timer;

public class Metric {

	private static final Logger LOGGER = LoggerFactory.getLogger(Metric.class);

	private JedisPoolWrapper jedisPool;

	public Metric(JedisPoolWrapper jedisPool) {
		this.jedisPool = jedisPool;
	}

	/**
	 * Request metrics related to a service definition key
	 * 
	 * @param key
	 *            A service definition key in the format of
	 *            host-service-serviceitem
	 * @param f
	 *            filter of field to retrieve
	 * @param q
	 * @param from
	 * @param to
	 * @return
	 */
	public Message metric(String key, Optional<String> f, Optional<String> q,
			Optional<String> from, Optional<String> to) throws ApiException {

		String decodedKey = null;
		Message message = null;

		try {
			decodedKey = URLDecoder.decode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Decoding of string {} failed- Returning the orignal",
					key, e);
			decodedKey = key;
		}

		try {
			FilterInf filter = FilterFactory.getFilter(decodedKey, jedisPool);

			QueryParms params = new QueryParms(f, q);
			FromTo fromto = new FromTo(from, to);

			message = filter.execute(params, fromto);
			message.setLinks(getStateAndNotificationLinks(decodedKey, jedisPool));

		} catch (IllegalArgumentException e) {
			throw new ApiException("from=" + from + "to=" + to, new ApiError(
					ErrorRef.FROMTO, "from=" + from + "to=" + to), e);
		}

		return message;
	}

	// //TODO testing POST on pubsub channel
	// //public PostStatus postMetric(String key, Map<String,String> param) {
	// public PostStatus postMetric(String key, @Param(value="request",
	// kind=Param.Kind.CONTEXT) RestxRequest request) {
	// long startTimer = System.currentTimeMillis();
	//
	//
	// //LOGGER.debug("Value {}", param.get("value"));
	// String body = null;
	// try(InputStreamReader isr = new
	// InputStreamReader(request.getContentStream())){
	// body = CharStreams.toString(isr);
	// System.out.println(body);
	// } catch (IOException e) {
	// LOGGER.error("Could not read request");
	// //TODO add exception
	// }
	//
	//
	// PostStatus postStatus = new PostStatus(key);
	// LOGGER.debug("{}",body);
	// String value = "0";//param.get("value");
	// if (value == null || value.isEmpty()) {
	// //Todo exception
	// return new PostStatus(key);
	// }
	//
	// try {
	// @SuppressWarnings("unused")
	// Float testFloat = new Float(value);
	// } catch (NumberFormatException ne) {
	// //TODO through a exeption
	// return new PostStatus("Failed");
	// }
	//
	// Jedis jedis = null;
	// try {
	// jedis = jedisPool.getResource();
	//
	// jedis.publish("channel", "{\"key\":\""+key+"\",\"value\":"+value);
	// } finally {
	// jedisPool.returnResource(jedis);
	// }
	// postStatus.setProcessingTime(System.currentTimeMillis() - startTimer);
	// return postStatus;
	// }

	private Set<Link> getStateAndNotificationLinks(String key,
			JedisPoolWrapper jedisPool2) {
		Jedis jedis = null;
		Set<String> stateLinks;
		Set<String> notificationLinks;

		Set<Link> links = new TreeSet<>();

		String servicekey = fullKey2ServiceKey(key);
		String itemPart = itemPartOfKey(key);
		try {
			jedis = jedisPool2.getResource();
			stateLinks = jedis.keys(LabelText.STATE_KEY + servicekey);
			notificationLinks = jedis.keys(LabelText.NOTIFICATION_KEY
					+ servicekey);

			if (!stateLinks.isEmpty()) {
				links.add(new Link(stateLinks.iterator().next(),
						LabelText.REL_STATE, LabelText.STATE_TITLE));
			}

			if (!notificationLinks.isEmpty()) {
				links.add(new Link(notificationLinks.iterator().next(),
						LabelText.REL_NOTIFICATION,
						LabelText.NOTIFICATION_TITLE));
			}

			// Find any related metric
			Set<String> metricLinks = null;
			metricLinks = jedis.keys(servicekey + "*");

			for (String link : metricLinks) {
				// Filter out aggregations
				if (!link.matches(".*/[H:D:W:M]/.*")) {
					if (key.equals(link)) {
						links.add(new Link(LabelText.METRIC_KEY + link,
								LabelText.REL_SELF, LabelText.METRIC_TITLE));
					} else {
						links.add(new Link(LabelText.METRIC_KEY + link,
								LabelText.REL_METRIC, LabelText.METRIC_TITLE));
					}
				}
			}

			// Find any related aggregations
			Set<String> aggregationLinks = null;
			aggregationLinks = jedis.keys(servicekey + "/[H:D:W:M]/*-"
					+ itemPart);

			for (String link : aggregationLinks) {
				links.add(new Link(LabelText.METRIC_KEY + link,
						LabelText.REL_AGGREGATION, LabelText.METRIC_TITLE));
			}

		} finally {
			jedisPool2.returnResource(jedis);
		}
		return links;
	}

	private String itemPartOfKey(String key) {
		String str = key.substring(key.lastIndexOf('-') + 1);
		return str;

	}

	private String fullKey2ServiceKey(String key) {
		String str = key.substring(0, key.lastIndexOf('-'));
		return str;

	}
}
