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

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.lang.Nullable;
import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.Tracer;
import org.springframework.observability.transport.http.HttpRequest;
import org.springframework.observability.transport.http.HttpResponse;

abstract class HttpTracingRecordingListener<REQ extends HttpRequest, RES extends HttpResponse>
		implements TracingRecordingListener {

	private final Tracer tracer;

	private final CurrentTraceContext currentTraceContext;

	private final Function<REQ, Span> startFunction;

	private final BiConsumer<RES, Span> stopConsumer;

	private final TracingTagFilter tracingTagFilter = new TracingTagFilter();

	private final TracingInstantRecorder tracingInstantRecorder;

	HttpTracingRecordingListener(Tracer tracer, Function<REQ, Span> startFunction, BiConsumer<RES, Span> stopConsumer) {
		this.tracer = tracer;
		this.currentTraceContext = tracer.currentTraceContext();
		this.startFunction = startFunction;
		this.stopConsumer = stopConsumer;
		this.tracingInstantRecorder = new TracingInstantRecorder(tracer);
	}

	@Override
	public void onError(IntervalRecording intervalRecording) {

	}

	@Override
	public void onStart(IntervalRecording intervalRecording) {
		Span parentSpan = intervalRecording.getContext(this).getSpan();
		CurrentTraceContext.Scope scope = null;
		if (parentSpan != null) {
			scope = this.currentTraceContext.maybeScope(parentSpan.context());
		}
		IntervalEvent event = intervalRecording.getEvent();
		REQ request = getRequest(event);
		Span span = this.startFunction.apply(request);
		scope = this.currentTraceContext.newScope(span.context());
		intervalRecording.getContext(this).setSpanAndScope(span, scope);
	}

	@Override
	public void onRestore(IntervalRecording intervalRecording) {
		CurrentTraceContext.Scope scope = this.currentTraceContext
				.maybeScope(intervalRecording.getContext(this).getSpan().context());
		intervalRecording.getContext(this).setScope(scope);
	}

	@Override
	public Tracer getTracer() {
		return this.tracer;
	}

	abstract REQ getRequest(IntervalEvent event);

	@Override
	public void onStop(IntervalRecording intervalRecording) {
		Span span = intervalRecording.getContext(this).getSpan();
		this.tracingTagFilter.tagSpan(span, intervalRecording.getTags());
		span.name(getSpanName(intervalRecording.getEvent()));
		RES response = getResponse(intervalRecording.getEvent());
		error(response, span);
		this.stopConsumer.accept(response, span);
		cleanup(intervalRecording);
	}

	// @Override
	// public void record(InstantRecording instantRecording) {
	// // TODO: Throw an exception?
	// }

	abstract String getSpanName(IntervalEvent event);

	abstract RES getResponse(IntervalEvent event);

	private void error(@Nullable HttpResponse response, Span span) {
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
			// TODO: Move to a common place
			span.tag("error", String.valueOf(httpStatus));
		}
	}

	@Override
	public void recordInstant(InstantRecording instantRecording) {
		this.tracingInstantRecorder.record(instantRecording);
	}

}
