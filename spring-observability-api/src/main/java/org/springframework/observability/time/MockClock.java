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

package org.springframework.observability.time;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Clock implementation to simplify testing.
 *
 * Mostly from:
 * https://github.com/micrometer-metrics/micrometer/blob/main/micrometer-core/src/main/java/io/micrometer/core/instrument/MockClock.java
 *
 * @author Jon Schneider
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class MockClock implements Clock {

	// has to be non-zero to prevent divide-by-zeroes and other weird math results based
	// on the clock
	private long time = MILLISECONDS.toNanos(1);

	@Override
	public long wallTime() {
		return this.time;
	}

	@Override
	public long monotonicTime() {
		return this.time;
	}

	/**
	 * Returns the wallTime in the desired unit.
	 * @param unit The {@link TimeUnit} you want the {@link Clock#wallTime()} to be
	 * converted.
	 * @return {@link Clock#wallTime()} in the desired {@link TimeUnit}.
	 * @see Clock#wallTime()
	 */
	public long wallTimeIn(TimeUnit unit) {
		return unit.convert(this.time, NANOSECONDS);
	}

	/**
	 * Returns the monotonicTime in the desired unit.
	 * @param unit The {@link TimeUnit} you want the {@link Clock#monotonicTime()} to be
	 * converted.
	 * @return {@link Clock#monotonicTime()} in the desired {@link TimeUnit}.
	 * @see Clock#monotonicTime()
	 */
	public long monotonicTimeIn(TimeUnit unit) {
		return unit.convert(this.time, NANOSECONDS);
	}

	/**
	 * @param amount The amount to add to the current time.
	 * @param unit The unit of the amount to add to the current time.
	 * @return The new time (wallTime and monotonicTime are the same).
	 */
	public long add(long amount, TimeUnit unit) {
		time += unit.toNanos(amount);
		return time;
	}

	/**
	 * @param duration The duration to add to the current time.
	 * @return The new time (wallTime and monotonicTime are the same).
	 */
	public long add(Duration duration) {
		return add(duration.toNanos(), NANOSECONDS);
	}

	/**
	 * @param amount The amount of seconds to add to the current time.
	 * @return The new time (wallTime and monotonicTime are the same).
	 */
	public long addSeconds(long amount) {
		return add(amount, SECONDS);
	}

}
