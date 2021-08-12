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

import org.springframework.observability.core.http.HttpServerRequest;
import org.springframework.observability.core.http.HttpServerResponse;
import org.springframework.observability.event.Recording;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalHttpServerEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.lang.NonNull;
import org.springframework.observability.lang.Nullable;
import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.Tracer;
import org.springframework.observability.tracing.http.HttpServerHandler;

/**
 * {@link RecordingListener} that uses the Tracing API to record events for HTTP server
 * side.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class HttpServerTracingRecordingListener
		implements TracingRecordingListener<HttpServerTracingRecordingListener.TracingContext> {

	private final TracingInstantRecorder tracingInstantRecorder;

	private final CurrentTraceContext currentTraceContext;

	private final HttpServerHandler handler;

	/**
	 * @param tracer tracer
	 * @param currentTraceContext current trace context
	 * @param handler http server handler
	 */
	public HttpServerTracingRecordingListener(Tracer tracer, CurrentTraceContext currentTraceContext,
			HttpServerHandler handler) {
		this.tracingInstantRecorder = new TracingInstantRecorder(tracer);
		this.currentTraceContext = currentTraceContext;
		this.handler = handler;
	}

	@Override
	public boolean isApplicable(Recording<?, ?> recording) {
		return recording.getEvent() instanceof IntervalHttpServerEvent;
	}

	@Override
	public void onStart(IntervalRecording<TracingContext> intervalRecording) {
		IntervalEvent event = intervalRecording.getEvent();
		IntervalHttpServerEvent clientEvent = (IntervalHttpServerEvent) event;
		HttpServerRequest request = clientEvent.getRequest();
		Span span = this.handler.handleReceive(request);
		CurrentTraceContext.Scope scope = this.currentTraceContext.newScope(span.context());
		intervalRecording.getContext().setSpan(span);
		intervalRecording.getContext().setScope(scope);
	}

	@Override
	public void onStop(IntervalRecording<TracingContext> intervalRecording) {
		IntervalEvent event = intervalRecording.getEvent();
		IntervalHttpServerEvent clientEvent = (IntervalHttpServerEvent) event;
		Span span = intervalRecording.getContext().getSpan();
		intervalRecording.getTags().forEach(tag -> span.tag(tag.getKey(), tag.getValue()));
		span.name(clientEvent.getRequest().method());
		HttpServerResponse response = clientEvent.getResponse();
		error(response, span);
		this.handler.handleSend(response, span);
		intervalRecording.getContext().getScope().close();
	}

	private void error(@Nullable HttpServerResponse response, Span span) {
		if (response == null) {
			return;
		}
		int httpStatus = response.statusCode();
		Throwable error = response.error();
		if (error != null) {
			return;
		}
		if (httpStatus == 0) {
			return;
		}
		if (httpStatus < 100 || httpStatus > 399) {
			span.tag("error", String.valueOf(httpStatus));
		}
	}

	@Override
	public void onError(IntervalRecording<TracingContext> intervalRecording) {

	}

	@Override
	public void record(InstantRecording instantRecording) {
		this.tracingInstantRecorder.record(instantRecording);
	}

	@Override
	public TracingContext createContext() {
		return new TracingContext();
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
