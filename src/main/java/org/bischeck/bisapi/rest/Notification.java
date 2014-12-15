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

import java.util.Set;

import org.bischeck.bisapi.domain.Message;
import org.bischeck.bisapi.domain.StateMessage;
import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.ApiError.ErrorRef;
import org.bischeck.bisapi.rest.filters.FilterFactory;
import org.bischeck.bisapi.rest.filters.FilterInf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import redis.clients.jedis.Jedis;

public class Notification {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Notification.class);

    private static final String KEYHEAD = LabelText.NOTIFICATION_KEY;

    private JedisPoolWrapper jedisPool;


    public Notification(JedisPoolWrapper jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Message notification(String key, Optional<String> f, Optional<String> q,
            Optional<String> from, Optional<String> to) throws ApiException {

    	Message message = null;
        try {
            FilterInf filter = FilterFactory
                    .getFilter(KEYHEAD + key, jedisPool);

            QueryParms params = new QueryParms(f, q);
            FromTo fromto = new FromTo(from, to);

            message = filter.execute(params, fromto);
            message.setLinks(Util.getAllMetricLinks(key,
                    LabelText.NOTIFICATION_KEY, jedisPool));
           
        } catch (IllegalArgumentException e) {
        	LOGGER.info("Illegal from {} to {} specification", from, to, e);
			throw new ApiException("from=" + from + "to=" + to, new ApiError(
					ErrorRef.FROMTO, "from=" + from + "to=" + to), e);
        }

        return message;
    }

    
    public StateMessage notifications(String notificationLevel) {
        StateMessage stateMesg = new StateMessage();

        Set<String> res = null;
        switch (notificationLevel.toLowerCase()) {
        case "alert":
            res = getNotificationFromCache(notificationLevel);
            stateMesg.setAlerts(res);
            break;
        default:
            res = getNotificationFromCache("alert");
            stateMesg.setAlerts(res);
            break;
        }

        return stateMesg;
    }

    private Set<String> getNotificationFromCache(String stateLevel) {

        Set<String> res = null;
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            res = jedis.smembers(stateLevel);
        } finally {
            jedisPool.returnResource(jedis);
        }
        return res;
    }

}
