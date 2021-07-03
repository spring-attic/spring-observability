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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.observability.event.SimpleRecorder;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.instant.NoOpInstantRecording;
import org.springframework.observability.event.instant.SimpleInstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.NoOpIntervalRecording;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.observability.test.TestInstantEvent.INSTANT_EVENT;
import static org.springframework.observability.test.TestIntervalEvent.INTERVAL_EVENT;

/**
 * @author Jonatan Ivanov
 */
@ExtendWith(MockitoExtension.class)
class SimpleRecorderTest {

	@Mock
	private RecordingListener<Void> listener;

	@Mock
	private Clock clock;

	@InjectMocks
	private SimpleRecorder<Void> recorder;

	@Test
	void shouldReturnSimpleIntervalRecordingByDefault() {
		IntervalRecording<Void> recording = recorder.recordingFor(INTERVAL_EVENT);
		assertThat(recorder.isEnabled()).isTrue();
		assertThat(recording).isExactlyInstanceOf(SimpleIntervalRecording.class);
		assertThat(recording.getEvent()).isSameAs(INTERVAL_EVENT);
	}

	@Test
	void shouldReturnSimpleInstantRecordingByDefault() {
		InstantRecording recording = recorder.recordingFor(INSTANT_EVENT);
		assertThat(recorder.isEnabled()).isTrue();
		assertThat(recording).isExactlyInstanceOf(SimpleInstantRecording.class);
		assertThat(recording.getEvent()).isSameAs(INSTANT_EVENT);
	}

	@Test
	void shouldReturnNoOpIntervalRecordingIfDisabled() {
		assertThat(recorder.isEnabled()).isTrue();
		assertThat(recorder.recordingFor(INTERVAL_EVENT)).isExactlyInstanceOf(SimpleIntervalRecording.class);

		recorder.setEnabled(false);
		assertThat(recorder.isEnabled()).isFalse();
		assertThat(recorder.recordingFor(INTERVAL_EVENT)).isExactlyInstanceOf(NoOpIntervalRecording.class);

		recorder.setEnabled(true);
		assertThat(recorder.isEnabled()).isTrue();
		assertThat(recorder.recordingFor(INTERVAL_EVENT)).isExactlyInstanceOf(SimpleIntervalRecording.class);
	}

	@Test
	void shouldReturnSimpleInstantRecordingIfDisabled() {
		assertThat(recorder.isEnabled()).isTrue();
		assertThat(recorder.recordingFor(INSTANT_EVENT)).isExactlyInstanceOf(SimpleInstantRecording.class);

		recorder.setEnabled(false);
		assertThat(recorder.isEnabled()).isFalse();
		assertThat(recorder.recordingFor(INSTANT_EVENT)).isExactlyInstanceOf(NoOpInstantRecording.class);

		recorder.setEnabled(true);
		assertThat(recorder.isEnabled()).isTrue();
		assertThat(recorder.recordingFor(INSTANT_EVENT)).isExactlyInstanceOf(SimpleInstantRecording.class);
	}

}
