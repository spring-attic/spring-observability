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

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalHttpClientEvent;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.tracing.Tracer;
import org.springframework.observability.tracing.http.HttpClientHandler;
import org.springframework.observability.transport.http.HttpClientRequest;
import org.springframework.observability.transport.http.HttpClientResponse;

/**
 * {@link RecordingListener} that uses the Tracing API to record events for HTTP client
 * side.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 */
public class HttpClientTracingRecordingListener extends
		HttpTracingRecordingListener<HttpClientRequest, HttpClientResponse> implements TracingRecordingListener {

	/**
	 * Creates a new instance of {@link HttpClientTracingRecordingListener}.
	 * @param tracer tracer
	 * @param handler http client handler
	 */
	public HttpClientTracingRecordingListener(Tracer tracer, HttpClientHandler handler) {
		super(tracer, handler::handleSend, handler::handleReceive);
	}

	@Override
	public boolean isApplicable(Recording<?, ?> recording) {
		return recording.getEvent() instanceof IntervalHttpClientEvent;
	}

	@Override
	HttpClientRequest getRequest(IntervalEvent event) {
		IntervalHttpClientEvent clientEvent = (IntervalHttpClientEvent) event;
		return clientEvent.getRequest();
	}

	@Override
	String getSpanName(IntervalEvent event) {
		return getRequest(event).method();
	}

	@Override
	HttpClientResponse getResponse(IntervalEvent event) {
		IntervalHttpClientEvent clientEvent = (IntervalHttpClientEvent) event;
		return clientEvent.getResponse();
	}

}
