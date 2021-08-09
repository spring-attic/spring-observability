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

package org.springframework.observability.micrometer.listener;

import java.io.IOException;
import java.time.Duration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.instant.SimpleInstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.time.MockClock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;
import static org.springframework.observability.micrometer.listener.MicrometerRecordingListenerTest.TestInstantEvent.INSTANT_EVENT;
import static org.springframework.observability.micrometer.listener.MicrometerRecordingListenerTest.TestIntervalEvent.INTERVAL_EVENT;

public class MicrometerRecordingListenerTest {

	private static final MockClock CLOCK = new MockClock();

	private final MeterRegistry registry = new SimpleMeterRegistry();

	private final MicrometerRecordingListener listener = new MicrometerRecordingListener(registry);

	private IntervalRecording<Void> intervalRecording;

	private InstantRecording instantRecording;

	@BeforeEach
	void setUp() {
		intervalRecording = new SimpleIntervalRecording<>(INTERVAL_EVENT, listener, CLOCK);
		instantRecording = new SimpleInstantRecording(INSTANT_EVENT, listener, CLOCK);
		registry.forEachMeter(registry::remove);
	}

	@Test
	void onStopShouldRegisterTimer() {
		intervalRecording.start().tag(Tag.of("foo", "bar", LOW)).tag(Tag.of("userId", "12345", HIGH));
		CLOCK.addSeconds(3);
		intervalRecording.stop();

		Timer timer = registry.find(intervalRecording.getEvent().getLowCardinalityName()).tag("foo", "bar").timer();

		assertThat(registry.getMeters()).hasSize(1);
		assertThat(timer).isNotNull();
		assertThat(timer.count()).isEqualTo(1);
		assertThat(timer.totalTime(NANOSECONDS)).isEqualTo(Duration.ofSeconds(3).toNanos());
		assertThat(timer.max(NANOSECONDS)).isEqualTo(Duration.ofSeconds(3).toNanos());
	}

	@Test
	void recordShouldRegisterCounter() {
		instantRecording.tag(Tag.of("foo", "bar", LOW)).tag(Tag.of("userId", "12345", HIGH)).record();

		Counter counter = registry.find(instantRecording.getEvent().getLowCardinalityName()).tag("foo", "bar")
				.counter();

		assertThat(registry.getMeters()).hasSize(1);
		assertThat(counter).isNotNull();
		assertThat(counter.count()).isEqualTo(1);
	}

	@Test
	void onStartOrOnErrorShouldNotDoAnything() {
		intervalRecording.start().error(new IOException());
		assertThat(registry.getMeters()).isEmpty();
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
