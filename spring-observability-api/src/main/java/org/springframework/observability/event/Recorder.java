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

import java.util.List;

import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;

/**
 * A Recorder is basically a factory that creates {@link Recording} instances for your
 * {@link Event Events}. Implementations must make sure that none of the methods return
 * null.
 *
 * @param <T> context type
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface Recorder<T> {

	/**
	 * Creates a recording for an {@link IntervalEvent}.
	 * @param event an {@link IntervalEvent} to create a recording for
	 * @return an {@link IntervalRecording} for the provided {@link IntervalEvent}
	 */
	IntervalRecording recordingFor(IntervalEvent event);

	/**
	 * Creates a recording for an {@link InstantEvent}.
	 * @param event an {@link InstantEvent} to create a recording for
	 * @return an {@link InstantRecording} for the provided {@link InstantEvent}
	 */
	InstantRecording recordingFor(InstantEvent event);

	/**
	 * An indicator whether the recording is on/off.
	 * @return {@code true} when recording is enabled.
	 */
	boolean isEnabled();

	/**
	 * Turns the recording on/off.
	 * @param enabled {@code true} to enable recording.
	 */
	void setEnabled(boolean enabled);

	/**
	 * Returns the current recording.
	 * @return current recording or {@code null} when not present
	 */
	IntervalRecording getCurrentRecording();

	/**
	 * Sets the current recording.
	 * @param recording current recording to set
	 */
	void setCurrentRecording(IntervalRecording recording);

	/**
	 * Returns all registered recording customizers.
	 * @return recording customizers
	 */
	List<RecordingCustomizer> getRecordingCustomizers();

}
