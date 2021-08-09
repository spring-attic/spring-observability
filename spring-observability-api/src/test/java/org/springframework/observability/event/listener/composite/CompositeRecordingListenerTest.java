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

package org.springframework.observability.event.listener.composite;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.test.TestContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.test.TestIntervalEvent.INTERVAL_EVENT;

/**
 * @author Jonatan Ivanov
 */
@ExtendWith(MockitoExtension.class)
class CompositeRecordingListenerTest {

	@Mock
	private RecordingListener<TestContext> listener1;

	@Mock
	private RecordingListener<String> listener2;

	@Mock
	private RecordingListener<Void> listener3;

	@Captor
	private ArgumentCaptor<CompositeRecordingListener.IntervalRecordingView<TestContext>> captor1;

	@Captor
	private ArgumentCaptor<CompositeRecordingListener.IntervalRecordingView<String>> captor2;

	@Captor
	private ArgumentCaptor<CompositeRecordingListener.IntervalRecordingView<Void>> captor3;

	private final TestContext context1 = new TestContext();

	private final String context2 = "context2";

	private final Void context3 = null;

	@Mock
	private InstantRecording instantRecording;

	@Mock
	private IntervalRecording<CompositeContext> intervalRecording;

	private RecordingListener<CompositeContext> compositeListener;

	@BeforeEach
	void setUp() {
		compositeListener = new CompositeRecordingListener(listener1, listener2, listener3);
	}

	@Test
	void recordShouldDelegate() {
		compositeListener.record(instantRecording);

		verify(listener1).record(instantRecording);
		verify(listener2).record(instantRecording);
		verify(listener3).record(instantRecording);
	}

	@Test
	void onStartShouldDelegate() {
		when(listener1.createContext()).thenReturn(context1);
		when(listener2.createContext()).thenReturn(context2);
		when(listener3.createContext()).thenReturn(context3);

		compositeListener.onStart(intervalRecording);

		verify(listener1).onStart(captor1.capture());
		verify(listener2).onStart(captor2.capture());
		verify(listener3).onStart(captor3.capture());

		assertThatViewWrapsRecording(listener1, captor1.getValue(), intervalRecording);
		assertThatViewWrapsRecording(listener2, captor2.getValue(), intervalRecording);
		assertThatViewWrapsRecording(listener3, captor3.getValue(), intervalRecording);
	}

	@Test
	void onErrorShouldDelegate() {
		when(listener1.createContext()).thenReturn(context1);
		when(listener2.createContext()).thenReturn(context2);
		when(listener3.createContext()).thenReturn(context3);

		compositeListener.onError(intervalRecording);

		verify(listener1).onError(captor1.capture());
		verify(listener2).onError(captor2.capture());
		verify(listener3).onError(captor3.capture());

		assertThatViewWrapsRecording(listener1, captor1.getValue(), intervalRecording);
		assertThatViewWrapsRecording(listener2, captor2.getValue(), intervalRecording);
		assertThatViewWrapsRecording(listener3, captor3.getValue(), intervalRecording);
	}

	@Test
	void onStopShouldDelegate() {
		when(listener1.createContext()).thenReturn(context1);
		when(listener2.createContext()).thenReturn(context2);
		when(listener3.createContext()).thenReturn(context3);

		compositeListener.onStop(intervalRecording);

		verify(listener1).onStop(captor1.capture());
		verify(listener2).onStop(captor2.capture());
		verify(listener3).onStop(captor3.capture());

		assertThatViewWrapsRecording(listener1, captor1.getValue(), intervalRecording);
		assertThatViewWrapsRecording(listener2, captor2.getValue(), intervalRecording);
		assertThatViewWrapsRecording(listener3, captor3.getValue(), intervalRecording);
	}

	@Test
	void createContextShouldReturnComposite() {
		when(listener1.createContext()).thenReturn(context1);
		when(listener2.createContext()).thenReturn(context2);
		when(listener3.createContext()).thenReturn(context3);

		CompositeContext context = compositeListener.createContext();

		assertThat(context.byListener(listener1)).isSameAs(context1);
		assertThat(context.byListener(listener2)).isSameAs(context2);
		assertThat(context.byListener(listener3)).isSameAs(context3);
	}

	private <T> void assertThatViewWrapsRecording(RecordingListener<T> listener,
			CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		assertThatGetEventDelegates(recordingView, recording);
		assertThatGetHighCardinalityNameDelegates(recordingView, recording);
		assertThatHighCardinalityNameDelegates(recordingView, recording);
		assertThatGetTagsDelegates(recordingView, recording);
		assertThatTagsDelegates(recordingView, recording);
		assertThatGetDurationDelegates(recordingView, recording);
		assertThatGetStartNanosDelegates(recordingView, recording);
		assertThatGetStopNanosDelegates(recordingView, recording);
		assertThatGetStartWallTimeDelegates(recordingView, recording);
		assertThatStartDelegates(recordingView, recording);
		assertThatStopDelegates(recordingView, recording);
		assertThatGetErrorDelegates(recordingView, recording);
		assertThatErrorDelegates(recordingView, recording);
		assertThatGetContextDelegates(listener, recordingView, recording);
		assertThatToStringDelegates(recordingView, recording);

		reset(recording);
	}

	private <T> void assertThatGetEventDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		when(recording.getEvent()).thenReturn(INTERVAL_EVENT);
		IntervalEvent actualEvent = recordingView.getEvent();

		verify(recording).getEvent();
		assertThat(actualEvent).isSameAs(recording.getEvent());
	}

	private <T> void assertThatGetHighCardinalityNameDelegates(
			CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		when(recording.getHighCardinalityName()).thenReturn("12345");
		String actualHighCardinalityName = recordingView.getHighCardinalityName();

		verify(recording).getHighCardinalityName();
		assertThat(actualHighCardinalityName).isEqualTo("12345");
	}

	private <T> void assertThatHighCardinalityNameDelegates(
			CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		String highCardinalityName = "12345";
		when(recording.highCardinalityName(highCardinalityName)).thenReturn(recording);

		IntervalRecording<T> actualRecording = recordingView.highCardinalityName(highCardinalityName);
		verify(recording).highCardinalityName(highCardinalityName);
		assertThat(actualRecording).isSameAs(recording);
	}

	private <T> void assertThatGetTagsDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		Tag tag = Tag.of("a", "b", HIGH);
		when(recording.getTags()).thenReturn(Collections.singletonList(tag));

		Iterable<Tag> actualTags = recordingView.getTags();
		verify(recording).getTags();
		assertThat(actualTags).containsExactly(tag);
	}

	private <T> void assertThatTagsDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		Tag tag = Tag.of("a", "b", HIGH);
		when(recording.tag(tag)).thenReturn(recording);

		IntervalRecording<T> actualRecording = recordingView.tag(tag);
		verify(recording).tag(tag);
		assertThat(actualRecording).isSameAs(recording);
	}

	private <T> void assertThatGetDurationDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		Duration duration = Duration.ofMillis(42);
		when(recording.getDuration()).thenReturn(duration);

		Duration actualDuration = recordingView.getDuration();
		verify(recording).getDuration();
		assertThat(actualDuration).isSameAs(duration);
	}

	private <T> void assertThatGetStartNanosDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		long startNanos = 12;
		when(recording.getStartNanos()).thenReturn(startNanos);

		long actualStartNanos = recordingView.getStartNanos();
		verify(recording).getStartNanos();
		assertThat(actualStartNanos).isEqualTo(startNanos);
	}

	private <T> void assertThatGetStopNanosDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		long stopNanos = 42;
		when(recording.getStopNanos()).thenReturn(stopNanos);

		long actualStopNanos = recordingView.getStopNanos();
		verify(recording).getStopNanos();
		assertThat(actualStopNanos).isEqualTo(stopNanos);
	}

	private <T> void assertThatGetStartWallTimeDelegates(
			CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		long startWallTime = 12;
		when(recording.getStartWallTime()).thenReturn(startWallTime);

		long actualStartWallTime = recordingView.getStartWallTime();
		verify(recording).getStartWallTime();
		assertThat(actualStartWallTime).isEqualTo(startWallTime);
	}

	private <T> void assertThatStartDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		when(recording.start()).thenReturn(recording);

		IntervalRecording<T> actualRecording = recordingView.start();
		verify(recording).start();
		assertThat(actualRecording).isSameAs(recording);
	}

	private <T> void assertThatStopDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		recordingView.stop();
		verify(recording).start();
	}

	private <T> void assertThatGetErrorDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		Throwable error = new IOException("simulated");
		when(recording.getError()).thenReturn(error);

		Throwable actualError = recordingView.getError();
		verify(recording).getError();
		assertThat(actualError).isSameAs(error);
	}

	private <T> void assertThatErrorDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		Throwable error = new IOException("simulated");
		when(recording.error(error)).thenReturn(recording);

		IntervalRecording<T> actualRecording = recordingView.error(error);
		verify(recording).error(error);
		assertThat(actualRecording).isSameAs(recording);
	}

	private <T> void assertThatGetContextDelegates(RecordingListener<T> listener,
			CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		T context = listener.createContext();
		CompositeContext compositeContext = mock(CompositeContext.class);
		when(recording.getContext()).thenReturn(compositeContext);
		when(compositeContext.byListener(listener)).thenReturn(context);

		T actualContext = recordingView.getContext();
		verify(recording).getContext();
		assertThat(actualContext).isSameAs(context);
	}

	private <T> void assertThatToStringDelegates(CompositeRecordingListener.IntervalRecordingView<T> recordingView,
			IntervalRecording<CompositeContext> recording) {
		String toString = "{test}";
		when(recording.toString()).thenReturn(toString);

		String actualToString = recordingView.toString();
		// verify(recording).toString(); //Mockito cannot verify toString() :(
		assertThat(actualToString).isSameAs(toString);
	}

}
