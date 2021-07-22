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

/**
 * Represents the recording of an {@link IntervalEvent}.
 *
 * @param <T> Context Type
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface IntervalRecording<T> extends Recording<IntervalEvent, IntervalRecording<T>> {

	/**
	 * @return The duration of the event.
	 */
	Duration getDuration();

	/**
	 * @return The current name of the event.
	 */
	String getName();

	/**
	 * @return The start time in nanos. The value is only meaningful when compared with
	 * another value to determine the elapsed time.
	 */
	long getStartNanos();

	/**
	 * @return The stop time in nanos. The value is only meaningful when compared with
	 * another value to determine the elapsed time.
	 */
	long getStopNanos();

	/**
	 * @return The wall time (system time) in nanoseconds since the epoch at the time the
	 * event started. Should not be used to determine durations.
	 */
	long getStartWallTime();

	/**
	 * Signals the beginning of an {@link IntervalEvent}.
	 * @return itself.
	 */
	IntervalRecording<T> start();

	/**
	 * Signals the beginning of an {@link IntervalEvent} at a given time.
	 * @param nanos the start time in nanos
	 * @return itself.
	 */
	IntervalRecording<T> start(long nanos);

	/**
	 * Renames the {@link IntervalEvent}.
	 * @param name updated name
	 * @return itself.
	 */
	IntervalRecording<T> name(String name);

	/**
	 * Signals the end of an {@link IntervalEvent}.
	 * @param nanos the stop time in nanos
	 */
	void stop(long nanos);

	/**
	 * Signals the end of an {@link IntervalEvent}.
	 */
	void stop();

	/**
	 * @return The {@link Throwable} instance in case there was an error.
	 */
	Throwable getError();

	/**
	 * Sets the error to the recording.
	 * @param error The {@link Throwable} to set.
	 * @return itself.
	 */
	IntervalRecording<T> error(Throwable error);

	/**
	 * @return A context object in case you need to pass data between listener methods.
	 */
	T getContext();

}
