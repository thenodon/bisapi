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

import java.util.Optional;

public class FromTo {
    private static final Long FROM_DEFAULT = 0L;
    private static final Long TO_DEFAULT = 99L;

    private Long from;
    private Long to;
    private Long offset;

    public FromTo(Optional<String> from, Optional<String> to) {

        this.from = validateFrom(from);
        this.to = validateTo(this.from, to);
        if (this.from < 0 || this.to < -1) {
            throw new IllegalArgumentException(
                    "From must be  >= 0 or empty and to must be empty, -1 or >= 0");
        }
    }

    public FromTo(Integer from, Integer to) {
    	this(new Long(from),new Long(to));
    }

    public FromTo(Long from, Long to) {

        this.from = new Long(from);
        this.to = new Long(to);
        if (this.from < 0 || this.to < -1) {
            throw new IllegalArgumentException(
                    "From must be  >= 0 or empty and to must be empty, -1 or >= 0");
        }
        this.offset = this.to - this.from + 1;
    }

    public FromTo(FromTo fromto) {
        this.from = fromto.getFrom();
        this.to = fromto.getTo();
    }

    public FromTo(FromTo fromto, int inc) {
        this.from = fromto.getTo() + 1;
        this.to = this.from + inc;
    }

    private Long validateTo(Long from, Optional<String> to) {
        if (to.isPresent()) {
            String toStr = to.get().trim();

            if (checkOffSet(toStr)) {
                offset = Long.valueOf(toStr.substring(1).trim());
                return getCount(from, toStr);
            } else {
                offset = getPos(toStr) - from + 1;
                return getPos(toStr);
            }
        } else {
            offset = TO_DEFAULT + 1;
            return from + TO_DEFAULT;
        }
    }

    private Long validateFrom(Optional<String> from) {
        if (from.isPresent()) {
            return Long.valueOf(from.get().trim());
        } else {
            return FROM_DEFAULT;
        }
    }

    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    public Long hasOffset() {
        return offset;
    }

    private boolean checkOffSet(String toStr) {

        if (toStr.length() > 0 && toStr.charAt(0) == '+') {
            return true;
        }

        return false;
    }

    private Long getPos(String toStr) {
        return Long.valueOf(toStr);
    }

    private Long getCount(Long fromIndex, String toIndexStr) {
        return fromIndex + Long.valueOf(toIndexStr.substring(1).trim()) - 1;

    }

}
