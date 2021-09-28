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

import java.util.ArrayList;
import java.util.Collection;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.observability.event.interval.IntervalLongRunningHttpServerEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.time.MockClock;
import org.springframework.observability.transport.http.HttpServerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;

public class MicrometerLongRunningTaskRecordingListenerTest {

	private static final MockClock CLOCK = new MockClock();

	private final MeterRegistry registry = new SimpleMeterRegistry();

	private final MicrometerLongRunningTaskRecordingListener listener = new MicrometerLongRunningTaskRecordingListener(
			registry);

	private IntervalRecording<MicrometerLongRunningTaskRecordingListener.LongRunningTaskContext> intervalRecording;

	@BeforeEach
	void setUp() {
		intervalRecording = new SimpleIntervalRecording(new LongRunningEvent(), listener, CLOCK);
		registry.forEachMeter(registry::remove);
	}

	@Test
	void onIsApplicableShouldReturnTrueOnlyForLongRunningEvents() {
		assertThat(this.listener.isApplicable(this.intervalRecording)).isTrue();
		assertThat(this.listener.isApplicable(new SimpleIntervalRecording(() -> "", listener, CLOCK))).isFalse();
	}

	@Test
	void onStopShouldRegisterTimer() {
		intervalRecording.tag(Tag.of("foo", "bar", LOW)).tag(Tag.of("userId", "12345", HIGH)).start();
		intervalRecording.stop();

		LongTaskTimer timer = registry.find(intervalRecording.getEvent().getLowCardinalityName()).tag("foo", "bar")
				.longTaskTimer();

		assertThat(registry.getMeters()).hasSize(1);
		assertThat(timer).isNotNull();

		timer = registry.find(intervalRecording.getEvent().getLowCardinalityName()).tagKeys("userId").longTaskTimer();
		assertThat(timer).as("High cardinality tags should not be added").isNull();
	}

	static class LongRunningEvent extends IntervalLongRunningHttpServerEvent {

		LongRunningEvent() {
			super(new HttpServerRequest() {
				@Override
				public String method() {
					return "get";
				}

				@Override
				public String path() {
					return "/";
				}

				@Override
				public String url() {
					return "http://localhost:123/foo";
				}

				@Override
				public String header(String name) {
					return "";
				}

				@Override
				public Collection<String> headerNames() {
					return new ArrayList<>();
				}

				@Override
				public Object unwrap() {
					return null;
				}
			});
		}

		@Override
		public String getLowCardinalityName() {
			return "foo";
		}

	}

}
