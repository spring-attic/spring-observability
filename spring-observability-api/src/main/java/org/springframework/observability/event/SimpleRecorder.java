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

package org.springframework.observability.event;

import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.instant.NoOpInstantRecording;
import org.springframework.observability.event.instant.SimpleInstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.interval.NoOpIntervalRecording;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.listener.composite.CompositeContext;
import org.springframework.observability.time.Clock;

/**
 * Simple implementation of a {@link Recorder}.
 *
 * @param <T> context type
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class SimpleRecorder<T> implements Recorder<T> {

	private final RecordingListener<CompositeContext> listener;

	private final Clock clock;

	private final List<RecordingCustomizer> customizers;

	private volatile boolean enabled;

	private final ThreadLocal<IntervalRecording> threadLocal = new ThreadLocal<>();

	private final Deque<IntervalRecording> recordings = new LinkedBlockingDeque<>();

	/**
	 * Create a new {@link SimpleRecorder}.
	 * @param listener the listener that needs to be notified about the recordings
	 * @param clock the clock to be used
	 * @param customizers recording customizers to be used
	 */
	public SimpleRecorder(RecordingListener<CompositeContext> listener, Clock clock,
			List<RecordingCustomizer> customizers) {
		this.listener = listener;
		this.clock = clock;
		this.enabled = true;
		this.customizers = customizers;
	}

	@Override
	public IntervalRecording recordingFor(IntervalEvent event) {
		IntervalRecording recording = this.enabled
				? new SimpleIntervalRecording(event, this.listener, this.clock, this::remove)
				: new NoOpIntervalRecording();
		setCurrentRecording(recording);
		return recording;
	}

	@Override
	public InstantRecording recordingFor(InstantEvent event) {
		return this.enabled ? new SimpleInstantRecording(event, this.listener, this.clock) : new NoOpInstantRecording();
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setCurrentRecording(IntervalRecording recording) {
		if (!this.enabled) {
			return;
		}
		IntervalRecording old = this.threadLocal.get();
		if (old != null) {
			this.recordings.addFirst(old);
		}
		this.threadLocal.set(recording);
	}

	/**
	 * Returns the current interval recording or {@code null} if there's none.
	 * @return currently stored recording
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IntervalRecording getCurrentRecording() {
		if (!this.enabled) {
			return null;
		}
		return this.threadLocal.get();
	}

	/**
	 * Removes the current span from thread local and brings back the previous span to the
	 * current thread local.
	 */
	private void remove() {
		if (!this.enabled) {
			return;
		}
		this.threadLocal.remove();
		if (this.recordings.isEmpty()) {
			return;
		}
		try {
			IntervalRecording first = this.recordings.removeFirst();
			this.threadLocal.set(first);
		}
		catch (NoSuchElementException ex) {
		}
	}

	@Override
	public List<RecordingCustomizer> getRecordingCustomizers() {
		return this.customizers;
	}

}
