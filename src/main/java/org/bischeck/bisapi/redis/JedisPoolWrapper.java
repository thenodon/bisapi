package org.bischeck.bisapi.redis;

import redis.clients.jedis.Jedis;

public interface JedisPoolWrapper {

    /**
     * Get a connection resources from the pool
     * 
     * @return the connection
     */
    Jedis getResource();

    /**
     * Return the connection to the pool after usage
     * 
     * @param jedis
     */
    void returnResource(Jedis jedis);

    /**
     * Destroy the pool
     */
    void destroy();
}
