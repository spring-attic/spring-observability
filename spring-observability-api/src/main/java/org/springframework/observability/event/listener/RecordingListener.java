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

package org.springframework.observability.event.listener;

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.context.ContextFactory;
import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;

/**
 * Implementing this interface of this interface make it possible to listen to ongoing or
 * competed {@link Recording Recordings}.
 *
 * @param <T> Context Type
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface RecordingListener<T> extends ContextFactory<T> {

	/**
	 * @param recording recording
	 * @return {@code true} when this listener is applicable
	 */
	default boolean isApplicable(Recording<?, ?> recording) {
		return true;
	}

	/**
	 * Called after the recording was started.
	 * @param intervalRecording The recording that was started.
	 */
	void onStart(IntervalRecording<T> intervalRecording);

	/**
	 * Called after the recording was stopped.
	 * @param intervalRecording The recording that was stopped.
	 */
	void onStop(IntervalRecording<T> intervalRecording);

	/**
	 * Called after a {@link Throwable} was thrown during the recording.
	 * @param intervalRecording The recording that was in progress when the error
	 * happened.
	 */
	void onError(IntervalRecording<T> intervalRecording);

	/**
	 * Signals that an {@link InstantEvent} was recorded.
	 * @param instantRecording The recording that belongs to the recorded
	 * {@link InstantEvent}.
	 */
	void record(InstantRecording instantRecording);

}
