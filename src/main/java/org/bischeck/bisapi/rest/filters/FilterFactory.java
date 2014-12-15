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

import java.util.HashMap;
import java.util.Map;

import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.ApiException;
import org.bischeck.bisapi.rest.ApiError;
import org.bischeck.bisapi.rest.ApiError.ErrorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

/**
 * The FilterFactory create {@link FilterInf} compatible classes depending on
 * the Redis data structure. Currently supported is sorted set and linked lists.
 * Each implementation is required to manage query parameters of the following
 * format: <br>
 * <code>
 * ?from=2&to=10&q=state=="OK"&f=state
 * </code> <br>
 * For the exact behavior check the classes that implements {@link FilterInf}.
 * 
 */
public class FilterFactory {

	private static Map<String, FilterInf> filterCache = new HashMap<String, FilterInf>();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FilterFactory.class);

	private FilterFactory() {

	}

	/**
	 * Factory that provide the right filter class depending on the Redis
	 * structure that the key implements. Currently the method support sorted
	 * sets and linked lists. The filter class is only instanced the first time
	 * the the factory is called with the key. After that a cached object is
	 * used for the key.
	 * 
	 * @param key
	 *            - the key used in Redis
	 * @param jedisPool
	 *            - the jedis connection pool
	 * @return the {@link FilterInf} related to the key
	 * @throws ApiException
	 *             if the key does not exists or the key is related to a Redis
	 *             structure not supported
	 */
	public static synchronized FilterInf getFilter(String key,
			JedisPoolWrapper jedisPool) throws ApiException {
		if (filterCache.containsKey(key)) {
			LOGGER.debug("Found {} in cache", key);
			return filterCache.get(key);
		} else {
			// Check if key is sortedSet or linked list
			FilterInf filter = null;
			LOGGER.debug("Not found {} in cache", key);
			if (keyExists(jedisPool, key)) {
				switch (keyType(jedisPool, key)) {
				case "list":
					filter = new FilterListImpl(key, jedisPool);
					LOGGER.debug("Key {} resolve to filter {}", key, filter
							.getClass().getName());
					filterCache.put(key, filter);
					break;
				case "zset":
					filter = new FilterZsetImpl(key, jedisPool);
					LOGGER.debug("Key {} resolve to filter {}", key, filter
							.getClass().getName());
					filterCache.put(key, filter);
					break;
				default:
					throw new ApiException(key, new ApiError(
							ErrorRef.REDIS_DATA_STRUCTURE_NOT_SUPPORTED, key));
				}
			} else {
				throw new ApiException(key, new ApiError(
						ErrorRef.KEY_DO_NOT_EXISTS, key));
			}
			return filter;
		}
	}

	private static Boolean keyExists(JedisPoolWrapper jedisPool, String key) {

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

	private static String keyType(JedisPoolWrapper jedisPool, String key) {

		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.type(key);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

}
