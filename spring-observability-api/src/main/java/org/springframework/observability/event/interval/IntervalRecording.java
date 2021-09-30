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

import java.time.Duration;

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.lang.Nullable;

/**
 * Represents the recording of an {@link IntervalEvent}.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface IntervalRecording extends Recording<IntervalEvent, IntervalRecording>, AutoCloseable {

	/**
	 * The duration of the event.
	 * @return the duration
	 */
	Duration getDuration();

	/**
	 * The start time in nanos. The value is only meaningful when compared with another
	 * value to determine the elapsed time.
	 * @return the start time in nanos
	 */
	long getStartNanos();

	/**
	 * The stop time in nanos. The value is only meaningful when compared with another
	 * value to determine the elapsed time.
	 * @return the stop time in nanos
	 */
	long getStopNanos();

	/**
	 * The wall time (system time) in nanoseconds since the epoch at the time the event
	 * started. Should not be used to determine durations.
	 * @return the wall time (system time) in nanoseconds
	 */
	long getStartWallTime();

	/**
	 * Signals the beginning of an {@link IntervalEvent}.
	 * @return this
	 */
	IntervalRecording start();

	/**
	 * Restores the recording (e.g. puts objects in scope in a new thread).
	 * @return this
	 */
	IntervalRecording restore();

	/**
	 * Signals the beginning of an {@link IntervalEvent} at a given time.
	 * @param wallTime the wall time (system time) in nanoseconds since the epoch at the
	 * time the event started
	 * @param monotonicTime the start time in nanos. The value is only meaningful when
	 * compared with another value to determine the elapsed time
	 * @return itself
	 */
	IntervalRecording start(long wallTime, long monotonicTime);

	/**
	 * Signals that an {@link InstantEvent} happened.
	 * @param event instant event that happened
	 */
	void recordInstant(InstantEvent event);

	/**
	 * Signals the end of an {@link IntervalEvent}.
	 */
	void stop();

	/**
	 * Signals the end of an {@link IntervalEvent} at a given time.
	 * @param monotonicTime the stop time in nanos. The value is only meaningful when
	 * compared with another value to determine the elapsed time
	 */
	void stop(long monotonicTime);

	/**
	 * Returns an error.
	 * @return the {@link Throwable} instance in case there was an error
	 */
	@Nullable
	Throwable getError();

	/**
	 * Sets the error to the recording.
	 * @param error the {@link Throwable} to set
	 * @return this
	 */
	IntervalRecording error(Throwable error);

	/**
	 * Returns the context object for the actual listener in case you need to pass data
	 * between listener methods.
	 * @param listener the listener that created the context object
	 * @param <T> type of the context
	 * @return a context object
	 */
	<T> T getContext(RecordingListener<T> listener);

	@Override
	default void close() {
		stop();
	}

}
