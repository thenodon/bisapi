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

import java.util.Set;

import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.FromTo;

import redis.clients.jedis.Jedis;

/**
 * Implement the access of a Redis sorted set
 */
public class FilterZsetImpl extends FilterAbstract implements FilterInf {

    public FilterZsetImpl(String key, JedisPoolWrapper jedisPool) {
        super(key, jedisPool);
    }

    protected Set<String> readCacheSet(FromTo fromto) {
        Set<String> res = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            if (jedis.exists(key)) {
                res = jedis.zrevrange(key, fromto.getFrom(), fromto.getTo());
            }
        } finally {
            jedisPool.returnResource(jedis);
        }
        return res;
    }

}
