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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nfunk.jep.JEP;
import org.nfunk.jep.Variable;

public class JEPQuery {

    JEP jep;
    String queryStatement;
    private Set<String> variableNames;

    public JEPQuery(String queryStatement) {
        jep = new JEP();
        jep.setAllowUndeclared(true);
        jep.parseExpression(queryStatement);
        this.queryStatement = queryStatement;

        variableNames = new HashSet<String>();
        Enumeration<?> ele = jep.getSymbolTable().elements();

        while (ele.hasMoreElements()) {
            Object obj = ele.nextElement();
            if (obj instanceof Variable && ((Variable) obj).getValue() == null) {
                variableNames.add(((Variable) obj).getName());
            }
        }
    }

    public Set<String> getVariables() {
        return variableNames;
    }

    public Boolean execute(Map<String, String> variables)
            throws IllegalAccessException {

        for (String name : variables.keySet()) {
            jep.addVariable(name, getValidObject(name, variables));
        }

        Double ret = jep.getValue();
        if (ret.equals(Double.NaN) || !(ret.equals(1.0) || ret.equals(0.0))) {
            throw new IllegalAccessException(
                    "Your query statement returns NaN, must be a boolean operation");
        }

        if (ret == 1.0) {
            return true;
        } else {
            return false;
        }

    }

    public String toString() {
        return queryStatement;
    }

    private Object getValidObject(String name, Map<String, String> variables) {
        if (variables.get(name).charAt(0) == '"'
                && variables.get(name).charAt(variables.get(name).length() - 1) == '"') {
            return new String(variables.get(name).substring(1,
                    variables.get(name).length() - 1));
        } else {
            return Double.parseDouble(variables.get(name));
        }

    }
}
