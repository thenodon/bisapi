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



public class ApiException extends Exception {

    private static final long serialVersionUID = -8394870825616051739L;
    private ApiError error;
    
    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, ApiError error) {
        super(message);
        this.error = error;
    }

    
    public ApiException(String message, ApiError error, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiError getError() {
        return error;
    }
    
    
}
