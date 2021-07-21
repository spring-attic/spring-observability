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

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Clock abstraction to guide users and simplify testing.
 *
 * Mostly from:
 * https://github.com/micrometer-metrics/micrometer/blob/main/micrometer-core/src/main/java/io/micrometer/core/instrument/Clock.java
 *
 * @author Jon Schneider
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface Clock {

	/**
	 * Default clock implementation using standard JDK components.
	 */
	Clock SYSTEM = new Clock() {
		@Override
		public long wallTime() {
			Instant instant = java.time.Clock.systemUTC().instant();
			return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
		}

		@Override
		public long monotonicTime() {
			return System.nanoTime();
		}
	};

	/**
	 * Current wall time (system time) in nanoseconds since the epoch. Should not be used
	 * to determine durations.
	 * @return Wall time in nanoseconds
	 */
	long wallTime();

	/**
	 * Current time from a monotonic clock source. The value is only meaningful when
	 * compared with another value to determine the elapsed time for an operation. The
	 * difference between two samples has a unit of nanoseconds. The returned value is
	 * typically equivalent to System.nanoTime.
	 * @return Monotonic time in nanoseconds
	 */
	long monotonicTime();

}
