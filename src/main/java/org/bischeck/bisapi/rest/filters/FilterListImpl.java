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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.FromTo;

import redis.clients.jedis.Jedis;

/**
 * Implement the access of a Redis linked list.
 * 
 */
public class FilterListImpl extends FilterAbstract implements FilterInf {

    public FilterListImpl(String key, JedisPoolWrapper jedisPool) {
        super(key, jedisPool);
    }

    protected Set<String> readCacheSet(FromTo fromto) {
        List<String> res = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            if (jedis.exists(key)) {
                res = jedis.lrange(key, fromto.getFrom(), fromto.getTo());
            }
        } finally {
            jedisPool.returnResource(jedis);
        }
        return new TreeSet<String>(res).descendingSet();
    }

}
