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

import java.util.Iterator;
import java.util.Set;

public class Message extends ResponseAbstract implements ResponseInf {

    private Set<String> result = null;
    private String key = null;
    private Set<Link> links = null;
    private Long responseTime;

    public Message(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Set<Link> getLinks() {
        return links;
    }

    @Override
    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    @Override
    public Set<String> getResult() {
        return result;
    }

    @Override
    public void setResult(Set<String> res) {
        this.result = res;
    }

    @Override
    public Integer getCount() {
        return result.size();
    }

    @Override
    public Long getProcessingTime() {
        return responseTime;
    }

    @Override
    public void setProcessingTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public String toString() {

        StringBuilder strbu = new StringBuilder();
        strbu.append("{");

        strbu.append(KEY).append(getKey()).append("\",");
        strbu.append(COUNT).append(getCount()).append(',');
        strbu.append(PROCESSING_TIME).append(getProcessingTime()).append(',');
        strbu.append(RESULT_BEGIN);
        if (!result.isEmpty()) {
            Iterator<String> iter = result.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                if (!first) {
                    strbu.append(",");
                } else {
                    first = false;
                }
                strbu.append(iter.next());
            }
        }
        strbu.append(RESULT_END);

        strbu.append(',');

        strbu.append(LINK_BEGIN);
        if (links != null && !links.isEmpty()) {
            Iterator<Link> iter = links.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                if (!first) {
                    strbu.append(",");
                } else {
                    first = false;
                }
                strbu.append(iter.next().toString());
            }
        }
        strbu.append(LINK_END);

        strbu.append('}');
        return strbu.toString();
    }

}
