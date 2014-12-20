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
    private static final Integer FROM_DEFAULT = 0;
    private static final Integer TO_DEFAULT = 99;

    private Integer from;
    private Integer to;
    private Integer offset;

    public FromTo(Optional<String> from, Optional<String> to) {

        this.from = validateFrom(from);
        this.to = validateTo(this.from, to);
        if (this.from < 0 || this.to < -1) {
            throw new IllegalArgumentException(
                    "From must be  >= 0 or empty and to must be empty, -1 or >= 0");
        }
    }

    public FromTo(Integer from, Integer to) {

        this.from = from;
        this.to = to;
        if (this.from < 0 || this.to < -1) {
            throw new IllegalArgumentException(
                    "From must be  >= 0 or empty and to must be empty, -1 or >= 0");
        }
        this.offset = to - from + 1;
    }

    public FromTo(FromTo fromto) {
        this.from = fromto.getFrom();
        this.to = fromto.getTo();
    }

    public FromTo(FromTo fromto, int inc) {
        this.from = fromto.getTo() + 1;
        this.to = this.from + inc;
    }

    private Integer validateTo(Integer from, Optional<String> to) {
        if (to.isPresent()) {
            String toStr = to.get().trim();

            if (isOffSet(toStr)) {
                offset = Integer.valueOf(toStr.substring(1).trim());
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

    private Integer validateFrom(Optional<String> from) {
        if (from.isPresent()) {
            return Integer.valueOf(from.get().trim());
        } else {
            return FROM_DEFAULT;
        }
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }

    private boolean isOffSet(String toStr) {

        if (toStr.length() > 0 && toStr.charAt(0) == '+') {
            return true;
        }

        return false;
    }

    private Integer getPos(String toStr) {
        return Integer.valueOf(toStr);
    }

    private Integer getCount(Integer fromIndex, String toIndexStr) {
        return fromIndex + Integer.valueOf(toIndexStr.substring(1).trim()) - 1;

    }

    public Integer offset() {
        return offset;
    }
}
