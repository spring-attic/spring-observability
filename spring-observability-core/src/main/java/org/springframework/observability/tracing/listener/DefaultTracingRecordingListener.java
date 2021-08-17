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

import java.util.concurrent.TimeUnit;

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.SpanAndScope;
import org.springframework.observability.tracing.Tracer;

/**
 * {@link RecordingListener} that uses the Tracing API to record events.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class DefaultTracingRecordingListener
		implements TracingRecordingListener<DefaultTracingRecordingListener.TracingContext> {

	private final Tracer tracer;

	private final TracingInstantRecorder tracingInstantRecorder;

	private final TracingTagFilter tracingTagFilter = new TracingTagFilter();

	/**
	 * @param tracer The tracer to use to record events.
	 */
	public DefaultTracingRecordingListener(Tracer tracer) {
		this.tracer = tracer;
		this.tracingInstantRecorder = new TracingInstantRecorder(tracer);
	}

	@Override
	public void onStart(IntervalRecording<TracingContext> intervalRecording) {
		Span span = this.tracer.nextSpan().name(intervalRecording.getHighCardinalityName())
				.start(getStartTimeInMicros(intervalRecording));
		intervalRecording.getContext().setSpanAndScope(span, this.tracer.withSpan(span));
	}

	@Override
	public void onStop(IntervalRecording<TracingContext> intervalRecording) {
		SpanAndScope spanAndScope = intervalRecording.getContext().getSpanAndScope();
		Span span = spanAndScope.getSpan().name(intervalRecording.getHighCardinalityName());
		this.tracingTagFilter.tagSpan(span, intervalRecording.getTags());
		spanAndScope.getScope().close();
		span.end(getStopTimeInMicros(intervalRecording));
	}

	@Override
	public void onError(IntervalRecording<TracingContext> intervalRecording) {
		Span span = intervalRecording.getContext().getSpanAndScope().getSpan();
		span.error(intervalRecording.getError());
	}

	@Override
	public void record(InstantRecording instantRecording) {
		this.tracingInstantRecorder.record(instantRecording);
	}

	@Override
	public TracingContext createContext() {
		return new TracingContext();
	}

	private long getStartTimeInMicros(IntervalRecording<TracingContext> recording) {
		return TimeUnit.NANOSECONDS.toMicros(recording.getStartWallTime());
	}

	private long getStopTimeInMicros(IntervalRecording<TracingContext> recording) {
		return TimeUnit.NANOSECONDS.toMicros(recording.getStartWallTime() + recording.getDuration().toNanos());
	}

	static class TracingContext {

		private SpanAndScope spanAndScope;

		SpanAndScope getSpanAndScope() {
			return this.spanAndScope;
		}

		void setSpanAndScope(Span span, Tracer.SpanInScope spanInScope) {
			this.spanAndScope = new SpanAndScope(span, spanInScope);
		}

	}

}
