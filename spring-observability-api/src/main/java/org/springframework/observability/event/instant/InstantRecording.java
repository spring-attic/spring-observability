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

package org.springframework.observability.event.instant;

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.listener.RecordingListener;

/**
 * Represents the recording of an {@link InstantEvent}. Calling the
 * {@link InstantRecording#record()} method should result in a call of
 * {@link RecordingListener#record(InstantRecording)}.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface InstantRecording extends Recording<InstantEvent, InstantRecording> {

	/**
	 * Signals that an {@link InstantEvent} happened.
	 */
	void record();

	/**
	 * Signals that an {@link InstantEvent} happened at a given time.
	 * @param wallTime The wall time (system time) in nanoseconds since the epoch at the
	 * time the event happened.
	 */
	void record(long wallTime);

	/**
	 * @return The wall time (system time) in nanoseconds since the epoch at the time the
	 * event happened.
	 */
	long getWallTime();

}
