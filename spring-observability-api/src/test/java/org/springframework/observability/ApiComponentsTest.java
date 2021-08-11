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

package org.springframework.observability;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.observability.event.Recorder;
import org.springframework.observability.event.SimpleRecorder;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.instant.NoOpInstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.NoOpIntervalRecording;
import org.springframework.observability.event.listener.composite.CompositeContext;
import org.springframework.observability.event.listener.composite.RunAllCompositeRecordingListener;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.test.TestContext;
import org.springframework.observability.test.TestRecordingListener;
import org.springframework.observability.time.MockClock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;
import static org.springframework.observability.test.TestInstantEvent.INSTANT_EVENT;
import static org.springframework.observability.test.TestIntervalEvent.INTERVAL_EVENT;

/**
 * @author Jonatan Ivanov
 */
class ApiComponentsTest {

	private final MockClock clock = new MockClock();

	private final TestRecordingListener listener = new TestRecordingListener(clock);

	private final Recorder<CompositeContext> recorder = new SimpleRecorder<>(
			new RunAllCompositeRecordingListener(listener), clock);

	@BeforeEach
	void setUp() {
		recorder.setEnabled(true);
		listener.reset();
	}

	@Test
	void shouldRecordInstantEvent() {
		recorder.recordingFor(INSTANT_EVENT).highCardinalityName(INSTANT_EVENT.getLowCardinalityName() + "-12345")
				.tag(Tag.of("testKey1", "testValue1", LOW)).tag(Tag.of("testKey2", "testValue2", HIGH)).record();

		InstantRecording recording = listener.getInstantRecording();
		assertThat(recording.getEvent()).isSameAs(INSTANT_EVENT);
		assertThat(recording.getHighCardinalityName()).isEqualTo("test-instant-event-12345");
		assertThat(recording.getTags()).containsExactly(Tag.of("testKey1", "testValue1", LOW),
				Tag.of("testKey2", "testValue2", HIGH));
		assertThat(recording.getWallTime()).isEqualTo(clock.wallTime());
		assertThat(recording.toString()).contains("event=test-instant-event")
				.contains("highCardinalityName=test-instant-event-12345")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}]");
	}

	@Test
	void shouldRecordInstantEventWithProvidedTime() {
		Recorder<CompositeContext> recorder = new SimpleRecorder<>(new RunAllCompositeRecordingListener(listener),
				null);
		recorder.recordingFor(INSTANT_EVENT).highCardinalityName(INSTANT_EVENT.getLowCardinalityName() + "-12345")
				.tag(Tag.of("testKey1", "testValue1", LOW)).tag(Tag.of("testKey2", "testValue2", HIGH)).record(100);

		InstantRecording recording = listener.getInstantRecording();
		assertThat(recording.getEvent()).isSameAs(INSTANT_EVENT);
		assertThat(recording.getHighCardinalityName()).isEqualTo("test-instant-event-12345");
		assertThat(recording.getTags()).containsExactly(Tag.of("testKey1", "testValue1", LOW),
				Tag.of("testKey2", "testValue2", HIGH));
		assertThat(recording.getWallTime()).isEqualTo(100);
		assertThat(recording.toString()).contains("event=test-instant-event")
				.contains("highCardinalityName=test-instant-event-12345")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}]");
	}

	@Test
	void shouldNotRecordInstantEventIfRecordingIsDisabled() {
		recorder.setEnabled(false);
		InstantRecording recording = recorder.recordingFor(INSTANT_EVENT)
				.highCardinalityName(INSTANT_EVENT.getLowCardinalityName() + "-12345")
				.tag(Tag.of("testKey1", "testValue1", LOW));
		recording.record();

		assertThat(recorder.isEnabled()).isFalse();
		assertThat(recording).isExactlyInstanceOf(NoOpInstantRecording.class);
		assertThat(listener.getInstantRecording()).isNull();

		assertThat(recording.getEvent().getLowCardinalityName()).isEqualTo("noop");
		assertThat(recording.getEvent().getDescription()).isEqualTo("noop");
		assertThat(recording.getHighCardinalityName()).isEqualTo("noop");
		assertThat(recording.getTags()).isEmpty();
		assertThat(recording.getWallTime()).isEqualTo(0);
		assertThat(recording).hasToString("NoOpInstantRecording");
	}

	@Test
	void shouldNotRecordInstantEventWithProvidedTimeIfRecordingIsDisabled() {
		Recorder<CompositeContext> recorder = new SimpleRecorder<>(new RunAllCompositeRecordingListener(listener),
				null);
		recorder.setEnabled(false);
		InstantRecording recording = recorder.recordingFor(INSTANT_EVENT)
				.highCardinalityName(INSTANT_EVENT.getLowCardinalityName() + "-12345")
				.tag(Tag.of("testKey1", "testValue1", LOW));
		recording.record(100);

		assertThat(recorder.isEnabled()).isFalse();
		assertThat(recording).isExactlyInstanceOf(NoOpInstantRecording.class);
		assertThat(listener.getInstantRecording()).isNull();

		assertThat(recording.getEvent().getLowCardinalityName()).isEqualTo("noop");
		assertThat(recording.getEvent().getDescription()).isEqualTo("noop");
		assertThat(recording.getHighCardinalityName()).isEqualTo("noop");
		assertThat(recording.getTags()).isEmpty();
		assertThat(recording.getWallTime()).isEqualTo(0);
		assertThat(recording).hasToString("NoOpInstantRecording");
	}

	@Test
	void shouldRecordIntervalEvent() {
		IntervalRecording<CompositeContext> recording = recorder.recordingFor(INTERVAL_EVENT)
				.tag(Tag.of("testKey1", "testValue1", LOW)).tag(Tag.of("testKey2", "testValue2", LOW)).start();

		verifyOnStart();

		try {
			clock.addSeconds(5);
			recording.tag(Tag.of("testKey3", "testValue3", HIGH));
			recording.error(new IOException("simulated"));

			verifyOnError();
		}
		finally {
			recording.highCardinalityName(INTERVAL_EVENT.getLowCardinalityName() + "-12345").stop();
			verifyOnStop();
		}
	}

	@Test
	void shouldRecordIntervalEventWithProvidedTime() {
		Recorder<CompositeContext> recorder = new SimpleRecorder<>(new RunAllCompositeRecordingListener(listener),
				null);
		IntervalRecording<CompositeContext> recording = recorder.recordingFor(INTERVAL_EVENT)
				.tag(Tag.of("testKey1", "testValue1", LOW)).tag(Tag.of("testKey2", "testValue2", LOW)).start(1, 2);

		verifyOnStart(1, 2);

		try {
			recording.tag(Tag.of("testKey3", "testValue3", HIGH));
			recording.error(new IOException("simulated"));

			verifyOnError(1, 2);
		}
		finally {
			recording.highCardinalityName(INTERVAL_EVENT.getLowCardinalityName() + "-12345")
					.stop(TimeUnit.MILLISECONDS.toNanos(100) + 2);
			verifyOnStop(1, 2, TimeUnit.MILLISECONDS.toNanos(100) + 2);
		}
	}

	@Test
	void shouldNotRecordIntervalEventIfRecordingIsDisabled() {
		recorder.setEnabled(false);
		IntervalRecording<CompositeContext> recording = recorder.recordingFor(INTERVAL_EVENT)
				.tag(Tag.of("testKey1", "testValue1", LOW)).start();

		try {
			clock.addSeconds(5);
			recording.error(new IOException("simulated"));
		}
		finally {
			recording.highCardinalityName(INTERVAL_EVENT.getLowCardinalityName()).stop();
		}

		assertThat(recorder.isEnabled()).isFalse();
		assertThat(recording).isExactlyInstanceOf(NoOpIntervalRecording.class);
		assertThat(listener.getOnStartRecording()).isNull();
		assertThat(listener.getOnStopRecording()).isNull();
		assertThat(listener.getOnErrorRecording()).isNull();

		assertThat(recording.getEvent().getLowCardinalityName()).isSameAs("noop");
		assertThat(recording.getEvent().getDescription()).isSameAs("noop");
		assertThat(recording.getHighCardinalityName()).isSameAs("noop");
		assertThat(recording.getDuration()).isSameAs(Duration.ZERO);
		assertThat(recording.getStartNanos()).isEqualTo(0);
		assertThat(recording.getStopNanos()).isEqualTo(0);
		assertThat(recording.getStartWallTime()).isEqualTo(0);

		assertThat(recording.getError()).isNull();
		assertThat(recording.getTags()).isEmpty();
		assertThat(recording.getContext()).isNull();
		assertThat(recording).hasToString("NoOpIntervalRecording");
	}

	@Test
	void shouldNotRecordIntervalEventWithProvidedTimeIfRecordingIsDisabled() {
		recorder.setEnabled(false);
		IntervalRecording<CompositeContext> recording = recorder.recordingFor(INTERVAL_EVENT)
				.tag(Tag.of("testKey1", "testValue1", LOW)).start(1, 2);

		try {
			recording.error(new IOException("simulated"));
		}
		finally {
			recording.highCardinalityName(INTERVAL_EVENT.getLowCardinalityName()).stop();
		}

		assertThat(recorder.isEnabled()).isFalse();
		assertThat(recording).isExactlyInstanceOf(NoOpIntervalRecording.class);
		assertThat(listener.getOnStartRecording()).isNull();
		assertThat(listener.getOnStopRecording()).isNull();
		assertThat(listener.getOnErrorRecording()).isNull();

		assertThat(recording.getEvent().getLowCardinalityName()).isSameAs("noop");
		assertThat(recording.getEvent().getDescription()).isSameAs("noop");
		assertThat(recording.getHighCardinalityName()).isSameAs("noop");
		assertThat(recording.getDuration()).isSameAs(Duration.ZERO);
		assertThat(recording.getStartNanos()).isEqualTo(0);
		assertThat(recording.getStopNanos()).isEqualTo(0);
		assertThat(recording.getStartWallTime()).isEqualTo(0);

		assertThat(recording.getError()).isNull();
		assertThat(recording.getTags()).isEmpty();
		assertThat(recording.getContext()).isNull();
		assertThat(recording).hasToString("NoOpIntervalRecording");
	}

	private void verifyOnStart() {
		verifyOnStart(listener.getOnStartSnapshot().wallTime(), listener.getOnStartSnapshot().monotonicTime());
	}

	private void verifyOnStart(long startWallTime, long startNanos) {
		IntervalRecording<TestContext> recording = listener.getOnStartRecording();

		assertThat(listener.getOnErrorRecording()).isNull();
		assertThat(listener.getOnStopRecording()).isNull();

		assertThat(recording.getEvent()).isSameAs(INTERVAL_EVENT);
		assertThat(recording.getHighCardinalityName()).isEqualTo("test-interval-event");
		assertThat(recording.getDuration()).isSameAs(Duration.ZERO);
		assertThat(recording.getStartNanos()).isEqualTo(startNanos);
		assertThat(recording.getStopNanos()).isEqualTo(0);
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);

		assertThat(recording.getError()).isNull();
		assertThat(recording.getTags()).containsExactly(Tag.of("testKey1", "testValue1", LOW),
				Tag.of("testKey2", "testValue2", LOW));
		assertThat(recording.getContext()).isSameAs(listener.getContext());
		assertThat(recording.toString()).contains("event=test-interval-event")
				.contains("highCardinalityName=test-interval-event")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}]").contains("duration=0ms")
				.contains("error=null");
	}

	private void verifyOnError() {
		verifyOnError(listener.getOnStartSnapshot().wallTime(), listener.getOnStartSnapshot().monotonicTime());
	}

	private void verifyOnError(long startWallTime, long startNanos) {
		IntervalRecording<TestContext> recording = listener.getOnErrorRecording();

		assertThat(listener.getOnStartRecording()).isNotNull();
		assertThat(listener.getOnStopRecording()).isNull();

		assertThat(recording.getEvent()).isSameAs(INTERVAL_EVENT);
		assertThat(recording.getHighCardinalityName()).isEqualTo("test-interval-event");
		assertThat(recording.getDuration()).isSameAs(Duration.ZERO);
		assertThat(recording.getStartNanos()).isEqualTo(startNanos);
		assertThat(recording.getStopNanos()).isEqualTo(0);
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);

		assertThat(recording.getError()).isExactlyInstanceOf(IOException.class).hasMessage("simulated").hasNoCause();
		assertThat(recording.getTags()).containsExactly(Tag.of("testKey1", "testValue1", LOW),
				Tag.of("testKey2", "testValue2", LOW), Tag.of("testKey3", "testValue3", HIGH));
		assertThat(recording.getContext()).isSameAs(listener.getContext());
		assertThat(recording.toString()).contains("event=test-interval-event")
				.contains("highCardinalityName=test-interval-event")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}, tag{testKey3=testValue3}]")
				.contains("duration=0ms").contains("error=java.io.IOException: simulated");
	}

	private void verifyOnStop() {
		verifyOnStop(listener.getOnStartSnapshot().wallTime(), listener.getOnStartSnapshot().monotonicTime(),
				listener.getOnStopSnapshot().monotonicTime());
	}

	private void verifyOnStop(long startWallTime, long startNanos, long stopNanos) {
		IntervalRecording<TestContext> recording = listener.getOnStopRecording();
		Duration duration = Duration.ofNanos(stopNanos - startNanos);

		assertThat(listener.getOnStartRecording()).isNotNull();
		assertThat(listener.getOnErrorRecording()).isNotNull();

		assertThat(recording.getEvent()).isSameAs(INTERVAL_EVENT);
		assertThat(recording.getHighCardinalityName()).isEqualTo("test-interval-event-12345");
		assertThat(recording.getDuration()).isEqualTo(duration);
		assertThat(recording.getStartNanos()).isEqualTo(startNanos);
		assertThat(recording.getStopNanos()).isEqualTo(stopNanos);
		assertThat(recording.getStartWallTime()).isEqualTo(startWallTime);

		assertThat(recording.getError()).isExactlyInstanceOf(IOException.class).hasMessage("simulated").hasNoCause();
		assertThat(recording.getTags()).containsExactly(Tag.of("testKey1", "testValue1", LOW),
				Tag.of("testKey2", "testValue2", LOW), Tag.of("testKey3", "testValue3", HIGH));
		assertThat(recording.getContext()).isSameAs(listener.getContext());
		assertThat(recording.toString()).contains("event=test-interval-event")
				.contains("highCardinalityName=test-interval-event-12345")
				.contains("tags=[tag{testKey1=testValue1}, tag{testKey2=testValue2}, tag{testKey3=testValue3}]")
				.contains("duration=" + duration.toMillis() + "ms").contains("error=java.io.IOException: simulated");
	}

}
