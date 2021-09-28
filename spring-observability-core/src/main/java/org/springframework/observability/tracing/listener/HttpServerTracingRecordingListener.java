/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.observability.tracing.listener;

import io.micrometer.api.event.Recording;
import io.micrometer.api.event.interval.IntervalEvent;
import io.micrometer.api.event.interval.IntervalHttpServerEvent;
import io.micrometer.api.event.listener.RecordingListener;
import io.micrometer.api.instrument.tracing.Tracer;
import io.micrometer.api.instrument.tracing.http.HttpServerHandler;
import io.micrometer.api.instrument.transport.http.HttpResponse;
import io.micrometer.api.instrument.transport.http.HttpServerRequest;
import io.micrometer.api.instrument.transport.http.HttpServerResponse;

/**
 * {@link RecordingListener} that uses the Tracing API to record events for HTTP server
 * side.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 */
public class HttpServerTracingRecordingListener extends
		HttpTracingRecordingListener<HttpServerRequest, HttpServerResponse> implements TracingRecordingListener {

	/**
	 * Creates a new instance of {@link HttpServerTracingRecordingListener}.
	 *
	 * @param tracer tracer
	 * @param handler http server handler
	 */
	public HttpServerTracingRecordingListener(Tracer tracer, HttpServerHandler handler) {
		super(tracer, handler::handleReceive, handler::handleSend);
	}

	@Override
	public boolean isApplicable(Recording<?, ?> recording) {
		return recording.getEvent() instanceof IntervalHttpServerEvent;
	}

	@Override
	HttpServerRequest getRequest(IntervalEvent event) {
		IntervalHttpServerEvent serverEvent = (IntervalHttpServerEvent) event;
		return serverEvent.getRequest();
	}

	@Override
	String getSpanName(IntervalEvent event) {
		IntervalHttpServerEvent serverEvent = (IntervalHttpServerEvent) event;
		if (serverEvent.getResponse() != null) {
			return spanNameFromRoute(serverEvent.getResponse());
		}
		return serverEvent.getRequest().method();
	}

	// taken from Brave
	private String spanNameFromRoute(HttpResponse response) {
		int statusCode = response.statusCode();
		String method = response.method();
		if (method == null) {
			return null; // don't undo a valid name elsewhere
		}
		String route = response.route();
		if (route == null) {
			return null; // don't undo a valid name elsewhere
		}
		if (!"".equals(route)) {
			return method + " " + route;
		}
		return catchAllName(method, statusCode);
	}

	// taken from Brave
	private String catchAllName(String method, int statusCode) {
		switch (statusCode) {
			// from https://tools.ietf.org/html/rfc7231#section-6.4
			case 301:
			case 302:
			case 303:
			case 305:
			case 306:
			case 307:
				return method + " redirected";
			case 404:
				return method + " not_found";
			default:
				return null;
		}
	}

	@Override
	HttpServerResponse getResponse(IntervalEvent event) {
		IntervalHttpServerEvent serverEvent = (IntervalHttpServerEvent) event;
		return serverEvent.getResponse();
	}

}
