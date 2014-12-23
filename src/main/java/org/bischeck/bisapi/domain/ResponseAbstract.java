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

package org.bischeck.bisapi.domain;

public abstract class ResponseAbstract {

    static final String RESULT_BEGIN = "\"result\":[";
    static final String RESULT_END = "]";
    static final String LINK_BEGIN = "\"links\":[";
    static final String LINK_END = "]";
    static final String KEY = "\"key\":\"";
    static final String COUNT = "\"count\":";
    static final String PROCESSING_TIME = "\"processingTime\":";

    static final String OK = "ok";
    static final String CRITICAL = "critical";
    static final String WARNING = "warning";
    static final String UNKNOWN = "unknown";
    static final String ALERT = "alert";

}
