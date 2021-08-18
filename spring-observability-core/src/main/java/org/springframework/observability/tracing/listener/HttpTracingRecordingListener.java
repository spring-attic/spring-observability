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

import org.springframework.observability.core.http.HttpRequest;
import org.springframework.observability.core.http.HttpResponse;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.lang.Nullable;
import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.Span;

abstract class HttpTracingRecordingListener<CONTEXT, REQ extends HttpRequest, RES extends HttpResponse>
		implements TracingRecordingListener<CONTEXT> {

	private final CurrentTraceContext currentTraceContext;

	private final Function<REQ, Span> startFunction;

	private final BiConsumer<RES, Span> stopConsumer;

	private final TracingTagFilter tracingTagFilter = new TracingTagFilter();

	HttpTracingRecordingListener(CurrentTraceContext currentTraceContext, Function<REQ, Span> startFunction,
			BiConsumer<RES, Span> stopConsumer) {
		this.currentTraceContext = currentTraceContext;
		this.startFunction = startFunction;
		this.stopConsumer = stopConsumer;
	}

	@Override
	public void onError(IntervalRecording<CONTEXT> intervalRecording) {

	}

	@Override
	public void onStart(IntervalRecording<CONTEXT> intervalRecording) {
		IntervalEvent event = intervalRecording.getEvent();
		REQ request = getRequest(event);
		Span span = this.startFunction.apply(request);
		CurrentTraceContext.Scope scope = this.currentTraceContext.newScope(span.context());
		setSpanAndScope(intervalRecording.getContext(), span, scope);
	}

	abstract REQ getRequest(IntervalEvent event);

	abstract void setSpanAndScope(CONTEXT context, Span span, CurrentTraceContext.Scope scope);

	@Override
	public void onStop(IntervalRecording<CONTEXT> intervalRecording) {
		Span span = getSpanFromContext(intervalRecording.getContext());
		this.tracingTagFilter.tagSpan(span, intervalRecording.getTags());
		span.name(getRequestMethod(intervalRecording.getEvent()));
		RES response = getResponse(intervalRecording.getEvent());
		error(response, span);
		this.stopConsumer.accept(response, span);
		cleanup(intervalRecording.getContext());
	}

	@Override
	public void record(InstantRecording instantRecording) {

	}

	abstract String getRequestMethod(IntervalEvent event);

	abstract Span getSpanFromContext(CONTEXT context);

	abstract RES getResponse(IntervalEvent event);

	abstract void cleanup(CONTEXT context);

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

}
