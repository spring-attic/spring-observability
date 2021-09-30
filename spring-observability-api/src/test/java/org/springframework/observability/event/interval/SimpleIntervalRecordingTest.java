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

package org.springframework.observability.event.interval;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.listener.composite.CompositeContext;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.time.MockClock;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;
import static org.springframework.observability.test.TestIntervalEvent.INTERVAL_EVENT;

/**
 * @author Jonatan Ivanov
 */
class SimpleIntervalRecordingTest {

	private final MockClock clock = new MockClock();

	private final RecordingListener<CompositeContext> listener = mock(RecordingListener.class);

	@Test
	void shouldReturnThePassedEvent() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThat(recording.getEvent()).isSameAs(INTERVAL_EVENT);
	}

	@Test
	void shouldReturnTheRecordingWithHighCardinalityName() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThat(recording.getHighCardinalityName()).isSameAs(INTERVAL_EVENT.getLowCardinalityName());
		String highCardinalityName = INTERVAL_EVENT.getLowCardinalityName() + "-123456";
		recording.highCardinalityName(highCardinalityName);
		assertThat(recording.getHighCardinalityName()).isSameAs(highCardinalityName);
	}

	@Test
	void shouldHaveTagsWhenAdded() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThat(recording.getTags()).isEmpty();

		Tag tag1 = Tag.of("testKey1", "testValue1", LOW);
		Tag tag2 = Tag.of("testKey2", "testValue2", HIGH);
		Tag tag3 = Tag.of("testKey3", "testValue3", LOW);

		recording.tag(tag1);
		assertThat(recording.getTags()).containsExactly(tag1);

		recording.tag(tag2);
		assertThat(recording.getTags()).containsExactly(tag1, tag2);

		recording.tag(tag3);
		assertThat(recording.getTags()).containsExactly(tag1, tag2, tag3);
	}

	@Test
	void shouldHaveErrorsWhenAddedAndEmitEvent() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock).start();
		assertThat(recording.getError()).isNull();

		Throwable error = new SocketTimeoutException("simulated");
		recording.error(error);

		verify(listener).onError(recording);
		assertThat(recording.getError()).isSameAs(error);
	}

	@Test
	void toStringShouldWork() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock)
				.tag(Tag.of("testKey1", "testValue1", LOW)).tag(Tag.of("testKey2", "testValue2", HIGH))
				.tag(Tag.of("testKey3", "testValue3", LOW)).start().error(new IOException("simulated"));

		assertThat(recording.toString()).contains("event=test-interval-event")
				.contains("highCardinalityName=test-interval-event").contains("duration=0ms")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}, tag{testKey3=testValue3}]")
				.contains("error=java.io.IOException: simulated");
		clock.addSeconds(1);
		recording.highCardinalityName(INTERVAL_EVENT.getLowCardinalityName() + "-123").stop();
		assertThat(recording.toString()).contains("event=test-interval-event")
				.contains("highCardinalityName=test-interval-event-123").contains("duration=1000ms")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}, tag{testKey3=testValue3}]")
				.contains("error=java.io.IOException: simulated");
	}

	@Test
	void startAndStopShouldRecordTimeAndEmitEvents() {
		long startMonotonicTime = clock.monotonicTime();
		long startWallTime = clock.wallTime();
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock).start()
				.error(new IOException("simulated"));

		verify(listener).onStart(recording);
		verify(listener).onError(recording);
		assertThat(recording.getStartNanos()).isEqualTo(startMonotonicTime);
		assertThat(recording.getStopNanos()).isEqualTo(0);
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);
		assertThat(recording.getDuration()).isEqualTo(Duration.ZERO);

		clock.addSeconds(3);
		recording.stop();

		verify(listener).onStop(recording);
		assertThat(recording.getStartNanos()).isEqualTo(startMonotonicTime);
		assertThat(recording.getStopNanos()).isEqualTo(startMonotonicTime + SECONDS.toNanos(3));
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);
		assertThat(recording.getDuration()).isEqualTo(Duration.ofSeconds(3));
	}

	@Test
	void startAndStopShouldRecordTimeAndEmitEventsWithProvidedTime() {
		long startMonotonicTime = 100;
		long stopMonotonicTime = startMonotonicTime + SECONDS.toNanos(3);
		long startWallTime = 1;
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, null)
				.start(startWallTime, startMonotonicTime).error(new IOException("simulated"));

		verify(listener).onStart(recording);
		verify(listener).onError(recording);
		assertThat(recording.getStartNanos()).isEqualTo(startMonotonicTime);
		assertThat(recording.getStopNanos()).isEqualTo(0);
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);
		assertThat(recording.getDuration()).isEqualTo(Duration.ZERO);

		recording.stop(stopMonotonicTime);

		verify(listener).onStop(recording);
		assertThat(recording.getStartNanos()).isEqualTo(startMonotonicTime);
		assertThat(recording.getStopNanos()).isEqualTo(stopMonotonicTime);
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);
		assertThat(recording.getDuration()).isEqualTo(Duration.ofSeconds(3));
	}

	@Test
	@Disabled("What do we do about this?")
	void doubleStartIsNotAllowed() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThatThrownBy(() -> recording.start().start()).isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording has already been started").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void doubleStartIsNotAllowedWithProvidedTime() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThatThrownBy(() -> recording.start(1, 1).start(1, 1)).isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording has already been started").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void stopBeforeStartIsNotAllowed() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThatThrownBy(recording::stop).isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording hasn't been started").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void stopBeforeStartIsNotAllowedWithProvidedTime() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThatThrownBy(() -> recording.stop(0)).isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording hasn't been started").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void tagAfterStopIsNotAllowed() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock).start();
		recording.stop();

		assertThatThrownBy(() -> recording.tag(Tag.of("testKey", "testValue", LOW)))
				.isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording has already been stopped").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void tagAfterStopIsNotAllowedWithProvidedTime() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock).start(1, 1);
		recording.stop(2);

		assertThatThrownBy(() -> recording.tag(Tag.of("testKey", "testValue", LOW)))
				.isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording has already been stopped").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void errorBeforeStartIsNotAllowed() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock);
		assertThatThrownBy(() -> recording.error(new IOException("simulated")))
				.isExactlyInstanceOf(IllegalStateException.class).hasMessage("IntervalRecording hasn't been started")
				.hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void errorAfterStopIsNotAllowed() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock).start();
		recording.stop();

		assertThatThrownBy(() -> recording.error(new IOException("simulated")))
				.isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording has already been stopped").hasNoCause();
	}

	@Test
	@Disabled("What do we do about this?")
	void errorAfterStopIsNotAllowedWithProvidedTime() {
		IntervalRecording recording = new SimpleIntervalRecording(INTERVAL_EVENT, listener, clock).start(1, 1);
		recording.stop(2);

		assertThatThrownBy(() -> recording.error(new IOException("simulated")))
				.isExactlyInstanceOf(IllegalStateException.class)
				.hasMessage("IntervalRecording has already been stopped").hasNoCause();
	}

}
