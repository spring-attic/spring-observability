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

import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;

/**
 * A Recorder is basically a factory that creates {@link Recording} instances for your
 * {@link Event Events}. Implementations must make sure that none of the methods return
 * null.
 *
 * @param <T> Context Type
 * @author Jonatan Ivanov
 */
public interface Recorder<T> {

	/**
	 * @param event An {@link IntervalEvent} to create a recording for.
	 * @return An {@link IntervalRecording} for the provided {@link IntervalEvent}.
	 */
	IntervalRecording<T> recordingFor(IntervalEvent event);

	/**
	 * @param event An {@link InstantEvent} to create a recording for.
	 * @return An {@link InstantRecording} for the provided {@link InstantEvent}.
	 */
	InstantRecording recordingFor(InstantEvent event);

	/**
	 * @return An indicator whether the recording is on/off.
	 */
	boolean isEnabled();

	/**
	 * @param enabled Turns the recording on/off.
	 */
	void setEnabled(boolean enabled);

}
