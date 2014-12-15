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
package org.bischeck.bisapi.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Provide a wrapper of JedisPool to enable specific pool configuration.
 */
public class JedisPoolSingleInstance implements JedisPoolWrapper {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(JedisPoolSingleInstance.class);

    private JedisPool jedispool;

    /**
     * Create the jedis connection pool
     * 
     * @param redisserver
     *            name of the server running the redis server- IP or FQDN
     * @param redisport
     *            the server socket port the redis server is listening on
     * @param redistimeout
     *            the connection timeout when connecting to redis server
     * @param redisauth
     *            the authentication token used when connecting to redis server
     * @param redisdb
     *            - the number of the redis database
     */
    public JedisPoolSingleInstance(String redisserver, Integer redisport,
            Integer redistimeout, String redisauth, Integer redisdb,
            Integer maxPoolSize) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        LOGGER.info("Connect to Redis at server {} port {} db {}", redisserver,
                redisport, redisdb);
        LOGGER.info("Max active: {} Max Idel: {} When exhusted: {}",
                poolConfig.getMaxTotal(), poolConfig.getMaxIdle());
        jedispool = new JedisPool(poolConfig, redisserver, redisport,
                redistimeout, redisauth, redisdb);
    }

    @Override
    public Jedis getResource() {
        Jedis jedis = jedispool.getResource();
        if (jedis == null) {
            LOGGER.error("No pool resources available");
            throw new JedisConnectionException("No pool resources available");
        }
        return jedis;
    }

    @Override
    public void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedispool.returnResource(jedis);
        } else {
            LOGGER.warn("Tried to return a null object to the redis pool");
        }
    }

    @Override
    public void destroy() {
        jedispool.destroy();
    }
}
