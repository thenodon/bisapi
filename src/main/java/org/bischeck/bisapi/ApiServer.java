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
package org.bischeck.bisapi;

import org.bischeck.bisapi.domain.Key;
import org.bischeck.bisapi.domain.Message;
import org.bischeck.bisapi.domain.StateMessage;
import org.bischeck.bisapi.monitoring.MetricsManager;
import org.bischeck.bisapi.redis.JedisModule;
import org.bischeck.bisapi.redis.JedisPoolWrapper;
import org.bischeck.bisapi.rest.ApiException;
import org.bischeck.bisapi.rest.ApiError;
import org.bischeck.bisapi.rest.Keys;
import org.bischeck.bisapi.rest.LabelText;
import org.bischeck.bisapi.rest.Metric;
import org.bischeck.bisapi.rest.Notification;
import org.bischeck.bisapi.rest.State;


import com.codahale.metrics.Timer;
import com.google.common.base.Optional;

import static spark.Spark.*;

public class ApiServer {
	public static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
	public static final String WEB_APP_LOCATION = "src/main/webapp";
	private static String baseUrl;

	public static void main(String[] args) throws Exception {
		int port = Integer.valueOf(Optional.fromNullable(System.getenv("PORT"))
				.or("9080"));

		JedisPoolWrapper jedisPool = JedisModule.datasource();

		// TODO Hard code appUrl needs to be fixed
		System.setProperty("appUrl", System.getProperty("/api", "/api"));
		System.setProperty("restx.mode",
				System.getProperty("restx.mode", "dev"));
		System.setProperty("restx.app.package", "org.bischeck.bisapi");

		exception(ApiException.class, (e, request, response) -> {
			response.type("application/json");
			if (e instanceof ApiException
					&& ((ApiException) e).getError() != null) {
				response.body(((ApiException) e).getError().toString());
				response.status(((ApiException) e).getError().getStatus());
			} else {
				response.status(500);
				response.body(ApiError.defaultError());
			}
		});

		// exception(NullPointerException.class, (e, request, response) -> {
		//
		// response.type("application/json");
		// response.status(50);
		// response.body(ApiError.defaultError());
		//
		// });

		metricGet(jedisPool);
		stateGet(jedisPool);
		notificationGet(jedisPool);
		keysGet(jedisPool);

	}

	private static void metricGet(JedisPoolWrapper jedisPool) {
		get("/v1/metric/:key",
				(request, response) -> {

					final Timer timer = MetricsManager.getTimer(Metric.class,
							"GET-key");
					final Timer.Context ctx = timer.time();
					Message mesg = null;
					try {
						Metric metric = new Metric(jedisPool);
						Optional<String> f = Optional.fromNullable(request
								.queryParams("f"));
						Optional<String> q = Optional.fromNullable(request
								.queryParams("q"));
						Optional<String> from = Optional.fromNullable(request
								.queryParams("from"));
						Optional<String> to = Optional.fromNullable(request
								.queryParams("to"));

						mesg = metric.metric(request.params(":key"), f, q,
								from, to);

						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

	}

	private static void stateGet(JedisPoolWrapper jedisPool) {
		get("/v1/state/:key",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(State.class,
							"GET-key");
					final Timer.Context ctx = timer.time();
					Message mesg = null;
					try {
						State state = new State(jedisPool);
						Optional<String> f = Optional.fromNullable(request
								.queryParams("f"));
						Optional<String> q = Optional.fromNullable(request
								.queryParams("q"));
						Optional<String> from = Optional.fromNullable(request
								.queryParams("from"));
						Optional<String> to = Optional.fromNullable(request
								.queryParams("to"));

						mesg = state.state(request.params(":key"), f, q, from,
								to);

						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/state/status/all",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(State.class,
							"GET-all");
					final Timer.Context ctx = timer.time();

					StateMessage mesg = null;
					try {
						State state = new State(jedisPool);
						mesg = state.statusLevel("");
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/state/status/ok", (request, response) -> {
			final Timer timer = MetricsManager.getTimer(State.class, "GET-ok");
			final Timer.Context ctx = timer.time();

			StateMessage mesg = null;
			try {
				State state = new State(jedisPool);
				mesg = state.statusLevel("OK");
				response.type("application/json");
			} finally {
				Long duration = ctx.stop() / MetricsManager.TO_MILLI;
				if (mesg != null) {
					mesg.setProcessingTime(duration);
				}
			}
			return mesg.toString();
		});

		get("/v1/state/status/critical",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(State.class,
							"GET-critical");
					final Timer.Context ctx = timer.time();

					StateMessage mesg = null;
					try {
						State state = new State(jedisPool);
						mesg = state.statusLevel("CRITICAL");
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/state/status/warning",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(State.class,
							"GET-warning");
					final Timer.Context ctx = timer.time();

					StateMessage mesg = null;
					try {
						State state = new State(jedisPool);
						mesg = state.statusLevel("WARNING");
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/state/status/unknown",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(State.class,
							"GET-unknown");
					final Timer.Context ctx = timer.time();

					StateMessage mesg = null;
					try {
						State state = new State(jedisPool);
						mesg = state.statusLevel("UNKNOWN");
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});
	}

	private static void notificationGet(JedisPoolWrapper jedisPool) {
		get("/v1/notification/:key",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(
							Notification.class, "GET-key");
					final Timer.Context ctx = timer.time();
					Message mesg = null;
					try {
						Notification notification = new Notification(jedisPool);
						Optional<String> f = Optional.fromNullable(request
								.queryParams("f"));
						Optional<String> q = Optional.fromNullable(request
								.queryParams("q"));
						Optional<String> from = Optional.fromNullable(request
								.queryParams("from"));
						Optional<String> to = Optional.fromNullable(request
								.queryParams("to"));

						mesg = notification.notification(
								request.params(":key"), f, q, from, to);

						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/notification/status/alerts",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(
							Notification.class, "GET-alert");
					final Timer.Context ctx = timer.time();

					StateMessage mesg = null;
					try {
						Notification notification = new Notification(jedisPool);
						mesg = notification.notifications("alert");
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();

				});
	}

	private static void keysGet(JedisPoolWrapper jedisPool) {
		get("/v1/keys/state",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(Keys.class,
							"GET-state");
					final Timer.Context ctx = timer.time();

					Key mesg = null;
					try {
						Keys keys = new Keys(jedisPool);
						mesg = keys.getByKey(LabelText.STATE_KEY,
								LabelText.ALL_KEYS);
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/keys/notification",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(Keys.class,
							"GET-notification");
					final Timer.Context ctx = timer.time();

					Key mesg = null;
					try {
						Keys keys = new Keys(jedisPool);
						mesg = keys.getByKey(LabelText.NOTIFICATION_KEY,
								LabelText.ALL_KEYS);
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});
		get("/v1/keys/metric",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(Keys.class,
							"GET-metric-all");
					final Timer.Context ctx = timer.time();

					Key mesg = null;
					try {
						Keys keys = new Keys(jedisPool);
						mesg = keys.getByKey(LabelText.METRIC_KEY,
								LabelText.ALL_KEYS);
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/keys/state/:key",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(Keys.class,
							"GET-state-key");
					final Timer.Context ctx = timer.time();

					Key mesg = null;
					try {
						Keys keys = new Keys(jedisPool);
						mesg = keys.getByKey(LabelText.STATE_KEY,
								request.params(":key"));
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/keys/notification/:key",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(Keys.class,
							"GET-notification-key");
					final Timer.Context ctx = timer.time();

					Key mesg = null;
					try {
						Keys keys = new Keys(jedisPool);
						mesg = keys.getByKey(LabelText.NOTIFICATION_KEY,
								request.params(":key"));
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();
				});

		get("/v1/keys/metric/:key",
				(request, response) -> {
					final Timer timer = MetricsManager.getTimer(Keys.class,
							"GET-metric-key");
					final Timer.Context ctx = timer.time();

					Key mesg = null;
					try {
						Keys keys = new Keys(jedisPool);
						mesg = keys.getByKey(LabelText.METRIC_KEY,
								request.params("key"));
						response.type("application/json");
					} finally {
						Long duration = ctx.stop() / MetricsManager.TO_MILLI;
						if (mesg != null) {
							mesg.setProcessingTime(duration);
						}
					}
					return mesg.toString();

				});
	}

	public static String getBaseUrl() {
		if (baseUrl == null) {
			baseUrl = System.getProperty("serverUrl")
					+ System.getProperty("appUrl");
		}
		return baseUrl;
	}
}
