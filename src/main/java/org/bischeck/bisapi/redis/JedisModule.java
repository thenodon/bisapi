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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JedisModule {
    private final static Logger LOGGER = LoggerFactory
            .getLogger(JedisModule.class);
    private final static String JEDIS_PROPERTIES_FILE = "jedis.properties";
    private static JedisPoolWrapper jedisPool;

    private JedisModule() {

    }

    public static synchronized JedisPoolWrapper datasource() {
        if (jedisPool == null) {

            URL bisapiResources = JedisModule.class.getClassLoader()
                    .getResource(JEDIS_PROPERTIES_FILE);

            Properties properties = new Properties();

            if (bisapiResources == null) {
                LOGGER.info(
                        "Property resource {} could not found - try default jedis connection",
                        JEDIS_PROPERTIES_FILE);
                jedisPool = new JedisPoolSingleInstance("localhost", 6379,
                        1000, null, 8, 50);
            } else {
                try (FileInputStream fin = new FileInputStream(
                        bisapiResources.getFile())) {
                    properties.load(fin);
                } catch (FileNotFoundException e) {
                    LOGGER.error(
                            "Property file {} not found - try default jedis connection",
                            JEDIS_PROPERTIES_FILE, e);
                    jedisPool = new JedisPoolSingleInstance("localhost", 6379,
                            1000, null, 8, 50);
                } catch (IOException e) {
                    LOGGER.error(
                            "Property file {} could be read - try default jedis connection",
                            JEDIS_PROPERTIES_FILE, e);
                    jedisPool = new JedisPoolSingleInstance("localhost", 6379,
                            1000, null, 8, 50);
                }

                if (properties.isEmpty()) {
                    LOGGER.error(
                            "Property file {} is empty - try default jedis connection",
                            JEDIS_PROPERTIES_FILE);
                    jedisPool = new JedisPoolSingleInstance("localhost", 6379,
                            1000, null, 8, 50);
                }
            }
        }
        return jedisPool;
    }

}
