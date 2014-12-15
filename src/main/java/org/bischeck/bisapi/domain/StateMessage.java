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

public class StateMessage extends ResponseAbstract {

    private Set<String> ok = null;
    private Set<String> critical = null;
    private Set<String> warning = null;
    private Set<String> unknown = null;
    private Set<String> alerts = null;

    private Long responseTime;
    private Set<Link> links;

    public Set<String> getCritcal() {
        return critical;
    }

    public void setCritical(Set<String> res) {
        this.critical = res;
    }

    public Set<String> getOkay() {
        return ok;
    }

    public void setOkay(Set<String> okay) {
        this.ok = okay;
    }

    public Set<String> getWarning() {
        return warning;
    }

    public void setWarning(Set<String> warning) {
        this.warning = warning;
    }

    public Set<String> getUnknown() {
        return unknown;
    }

    public void setUnknown(Set<String> unknown) {
        this.unknown = unknown;
    }

    public Set<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<String> alerts) {
        this.alerts = alerts;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public Integer getCount() {
        int count = 0;

        if (ok != null) {
            count += ok.size();
        }

        if (critical != null) {
            count += critical.size();
        }

        if (warning != null) {
            count += warning.size();
        }

        if (unknown != null) {
            count += unknown.size();
        }

        if (alerts != null) {
            count += alerts.size();
        }

        return count;
    }

    public Long getProcessingTime() {
        return responseTime;
    }

    public void setProcessingTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    public String toString() {
        StringBuilder strbu = new StringBuilder();
        strbu.append("{");

        strbu.append(COUNT).append(getCount()).append(',');
        strbu.append(PROCESSING_TIME).append(getProcessingTime()).append(',');

        strbu.append(RESULT_BEGIN);

        int count = getLevelCount() - 2;
        if (ok != null) {
            strbu.append(eachLevel(OK, ok));
            if (count > 0) {
                strbu.append(',');
                count--;
            }
        }

        if (critical != null) {
            strbu.append(eachLevel(CRITICAL, critical));
            if (count > 0) {
                strbu.append(',');
                count--;
            }
        }

        if (warning != null) {
            strbu.append(eachLevel(WARNING, warning));
            if (count > 0) {
                strbu.append(',');
                count--;
            }
        }

        if (unknown != null) {
            strbu.append(eachLevel(UNKNOWN, unknown));
            if (count > 0) {
                strbu.append(',');
                count--;
            }
        }

        if (alerts != null) {
            strbu.append(eachLevel(ALERT, alerts));
            if (count > 0) {
                strbu.append(',');
                count--;
            }
        }

        strbu.append(RESULT_END);

        if (links != null && !links.isEmpty()) {
            strbu.append(',');
            strbu.append(LINK_BEGIN);
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
            strbu.append(LINK_END);
        }

        strbu.append('}');
        return strbu.toString();
    }

    private StringBuilder eachLevel(String levelName, Set<String> levelSet) {
        StringBuilder strbu = new StringBuilder();

        if (levelSet != null && !levelSet.isEmpty()) {
            strbu.append("{\"").append(levelName).append("\":[");
            Iterator<String> iter = levelSet.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                if (!first) {
                    strbu.append(",");
                } else {
                    first = false;
                }
                strbu.append("\"").append(iter.next()).append("\"");
            }
            strbu.append("],\"count\":").append(levelSet.size()).append("}");
        }
        return strbu;
    }

    private int getLevelCount() {
        int countOfLevels = 0;
        if (ok != null) {
            countOfLevels++;
        }
        if (critical != null) {
            countOfLevels++;
        }
        if (warning != null) {
            countOfLevels++;
        }
        if (unknown != null) {
            countOfLevels++;
        }
        if (alerts != null) {
            countOfLevels++;
        }
        return countOfLevels;
    }
}
