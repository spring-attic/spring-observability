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

import org.springframework.observability.core.http.HttpClientRequest;
import org.springframework.observability.core.http.HttpClientResponse;
import org.springframework.observability.event.Recording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalHttpClientEvent;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.lang.NonNull;
import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.http.HttpClientHandler;

/**
 * {@link RecordingListener} that uses the Tracing API to record events for HTTP client
 * side.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class HttpClientTracingRecordingListener extends
		HttpTracingRecordingListener<HttpClientTracingRecordingListener.TracingContext, HttpClientRequest, HttpClientResponse>
		implements TracingRecordingListener<HttpClientTracingRecordingListener.TracingContext> {

	/**
	 * @param currentTraceContext current trace context
	 * @param handler http client handler
	 */
	public HttpClientTracingRecordingListener(CurrentTraceContext currentTraceContext, HttpClientHandler handler) {
		super(currentTraceContext, handler::handleSend, handler::handleReceive);
	}

	@Override
	public boolean isApplicable(Recording<?, ?> recording) {
		return recording.getEvent() instanceof IntervalHttpClientEvent;
	}

	@Override
	public TracingContext createContext() {
		return new TracingContext();
	}

	@Override
	HttpClientRequest getRequest(IntervalEvent event) {
		IntervalHttpClientEvent clientEvent = (IntervalHttpClientEvent) event;
		return clientEvent.getRequest();
	}

	@Override
	void setSpanAndScope(TracingContext tracingContext, Span span, CurrentTraceContext.Scope scope) {
		tracingContext.setSpan(span);
		tracingContext.setScope(scope);
	}

	@Override
	String getRequestMethod(IntervalEvent event) {
		return getRequest(event).method();
	}

	@Override
	Span getSpanFromContext(TracingContext context) {
		return context.getSpan();
	}

	@Override
	HttpClientResponse getResponse(IntervalEvent event) {
		IntervalHttpClientEvent clientEvent = (IntervalHttpClientEvent) event;
		return clientEvent.getResponse();
	}

	@Override
	void cleanup(TracingContext tracingContext) {
		tracingContext.getScope().close();
	}

	static class TracingContext {

		private Span span;

		private CurrentTraceContext.Scope scope;

		@NonNull
		Span getSpan() {
			return span;
		}

		void setSpan(Span span) {
			this.span = span;
		}

		@NonNull
		CurrentTraceContext.Scope getScope() {
			return scope;
		}

		void setScope(CurrentTraceContext.Scope scope) {
			this.scope = scope;
		}

	}

}
