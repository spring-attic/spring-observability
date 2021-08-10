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

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.instant.SimpleInstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.time.MockClock;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.Tracer;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;
import static org.springframework.observability.tracing.listener.TracingRecordingListenerTests.TestInstantEvent.INSTANT_EVENT;
import static org.springframework.observability.tracing.listener.TracingRecordingListenerTests.TestIntervalEvent.INTERVAL_EVENT;

@ExtendWith(MockitoExtension.class)
class TracingRecordingListenerTests {

	private static final MockClock CLOCK = new MockClock();

	@Mock
	private Tracer tracer;

	@Mock
	private Span span;

	@Mock
	private Tracer.SpanInScope spanInScope;

	private IntervalRecording<TracingRecordingListener.TracingContext> intervalRecording;

	private InstantRecording instantRecording;

	@InjectMocks
	private TracingRecordingListener listener;

	@BeforeEach
	void setUp() {
		intervalRecording = new SimpleIntervalRecording<>(INTERVAL_EVENT, listener, CLOCK);
		instantRecording = new SimpleInstantRecording(INSTANT_EVENT, listener);
	}

	@Test
	void onStartShouldStartTheSpan() {
		basicTracerAndSpanBehavior();
		when(span.start(CLOCK.wallTimeIn(MICROSECONDS))).thenReturn(span);
		intervalRecording.start();

		assertThat(intervalRecording.getContext().getSpanAndScope().getScope()).isSameAs(spanInScope);
		verify(tracer).nextSpan();
		verify(span).name(INTERVAL_EVENT.getLowCardinalityName());
		verify(span).start(CLOCK.wallTimeIn(MICROSECONDS));
	}

	@Test
	void onStopShouldEndTheSpan() {
		basicTracerAndSpanBehavior();
		when(span.start(CLOCK.wallTimeIn(MICROSECONDS))).thenReturn(span);

		intervalRecording.start().tag(Tag.of("foo", "bar", LOW)).tag(Tag.of("userId", "12345", HIGH));
		CLOCK.addSeconds(1);
		intervalRecording.stop();

		verify(span).tag("foo", "bar");
		verify(span).tag("userId", "12345");
		verify(intervalRecording.getContext().getSpanAndScope().getScope()).close();
		verify(span, times(0)).end();
		verify(span).end(CLOCK.wallTimeIn(MICROSECONDS));
	}

	@Test
	void onErrorShouldAddTheErrorToTheSpan() {
		basicTracerAndSpanBehavior();
		when(span.start(CLOCK.wallTimeIn(MICROSECONDS))).thenReturn(span);
		Throwable error = new IOException("simulated");

		intervalRecording.start().error(error);

		verify(span).error(error);
	}

	@Test
	void recordShouldNotDoAnythingWhenThereIsNoSpan() {
		when(tracer.currentSpan()).thenReturn(null);

		instantRecording.record();

		verifyNoInteractions(span);
		verifyNoMoreInteractions(tracer);
	}

	@Test
	void recordShouldAddEventToTheSpan() {
		basicTracerAndSpanBehavior();
		when(span.start(CLOCK.wallTimeIn(MICROSECONDS))).thenReturn(span);
		when(tracer.currentSpan()).thenReturn(span);
		intervalRecording.start();

		instantRecording.record();

		verify(span).event(instantRecording.getEvent().getLowCardinalityName());
	}

	@Test
	void recordShouldAddEventToTheSpanWithFixedTime() {
		basicTracerAndSpanBehavior();
		when(span.start(CLOCK.wallTimeIn(MICROSECONDS))).thenReturn(span);
		when(tracer.currentSpan()).thenReturn(span);
		intervalRecording.start();

		instantRecording.record(2000L);

		verify(span).event(2L, instantRecording.getEvent().getLowCardinalityName());
	}

	private void basicTracerAndSpanBehavior() {
		when(tracer.nextSpan()).thenReturn(span);
		when(tracer.withSpan(span)).thenReturn(spanInScope);
		when(span.name(INTERVAL_EVENT.getLowCardinalityName())).thenReturn(span);
	}

	enum TestIntervalEvent implements IntervalEvent {

		INTERVAL_EVENT("test-interval-event", "Test event to be able to record interval events");

		private final String name;

		private final String description;

		TestIntervalEvent(String name, String description) {
			this.name = name;
			this.description = description;
		}

		@Override
		public String getLowCardinalityName() {
			return this.name;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

	}

	enum TestInstantEvent implements InstantEvent {

		INSTANT_EVENT("test-instant-event", "Test event to be able to record instant events");

		private final String name;

		private final String description;

		TestInstantEvent(String name, String description) {
			this.name = name;
			this.description = description;
		}

		@Override
		public String getLowCardinalityName() {
			return this.name;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

	}

}
