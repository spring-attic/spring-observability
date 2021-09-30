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
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.instant.SimpleInstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.event.listener.composite.AllMatchingCompositeRecordingListener;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.time.MockClock;
import org.springframework.observability.tracing.test.simple.SimpleSpan;
import org.springframework.observability.tracing.test.simple.SimpleSpan.Event;
import org.springframework.observability.tracing.test.simple.SimpleTracer;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;
import static org.springframework.observability.tracing.listener.DefaultTracingRecordingListenerTests.TestInstantEvent.INSTANT_EVENT;
import static org.springframework.observability.tracing.listener.DefaultTracingRecordingListenerTests.TestIntervalEvent.INTERVAL_EVENT;

class DefaultTracingRecordingListenerTests {

	private static final MockClock CLOCK = new MockClock();

	private SimpleTracer tracer = new SimpleTracer();

	private IntervalRecording intervalRecording;

	private InstantRecording instantRecording;

	private DefaultTracingRecordingListener listener = new DefaultTracingRecordingListener(tracer);

	@BeforeEach
	void setUp() {
		intervalRecording = new SimpleIntervalRecording(INTERVAL_EVENT,
				new AllMatchingCompositeRecordingListener(Collections.singletonList(listener)), CLOCK);
		instantRecording = new SimpleInstantRecording(INSTANT_EVENT,
				new AllMatchingCompositeRecordingListener(Collections.singletonList(listener)), CLOCK);
	}

	@Test
	void onStartShouldStartTheSpan() {
		intervalRecording.start();

		SimpleSpan lastSpan = tracer.getLastSpan();
		assertThat(lastSpan.name).isEqualTo(INTERVAL_EVENT.getLowCardinalityName());
	}

	@Test
	void onStopShouldEndTheSpan() {
		intervalRecording.start().tag(Tag.of("foo", "bar", LOW)).tag(Tag.of("userId", "12345", HIGH));
		CLOCK.addSeconds(1);

		intervalRecording.stop();

		SimpleSpan onlySpan = tracer.getOnlySpan();
		assertThat(onlySpan.name).isEqualTo(INTERVAL_EVENT.getLowCardinalityName());
		assertThat(onlySpan.tags).containsEntry("foo", "bar").containsEntry("userId", "12345");
		assertThat(tracer.currentTraceContext().scopeClosed).isTrue();
	}

	@Test
	void onErrorShouldAddTheErrorToTheSpan() {
		Throwable error = new IOException("simulated");

		intervalRecording.start().error(error);

		assertThat(tracer.getLastSpan().throwable).isSameAs(error);
	}

	@Test
	void recordShouldNotDoAnythingWhenThereIsNoSpan() {
		instantRecording.recordInstant();

		assertThat(tracer.spans).isEmpty();
	}

	@Test
	void recordShouldAddEventToTheSpan() {
		CLOCK.addSeconds(1);
		tracer.nextSpan().start();
		intervalRecording.start();

		instantRecording.recordInstant();

		assertThat(tracer.getLastSpan().events)
				.containsOnly(new Event(instantRecording.getHighCardinalityName(), CLOCK.wallTimeIn(MICROSECONDS)));
	}

	@Test
	void recordShouldAddEventToTheSpanWithFixedTime() {
		tracer.nextSpan().start();
		intervalRecording.start();

		instantRecording.recordInstant(2_000);

		assertThat(tracer.getLastSpan().events).containsOnly(new Event(instantRecording.getHighCardinalityName(), 2));
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
