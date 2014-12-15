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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class QueryParms {

	List<String> filters;
	List<String> queries;

	public QueryParms(Optional<String> f, Optional<String> q) {
		filters = initList(f);
		queries = initList(q);
	}

	private List<String> initList(Optional<String> listStr) {
		List<String> list;
		if (listStr.isPresent()) {
			list = Arrays.asList(listStr.get().split("\\s*,\\s*"));
		} else {
			list = Arrays.asList();
		}
		return list;
	}

	public boolean hasFilters() {
		return !filters.isEmpty();
	}

	public boolean hasQuery() {
		return !queries.isEmpty();
	}

	public List<String> getFilters() {
		return filters;
	}

	public List<String> getQueries() {
		return queries;
	}

}
