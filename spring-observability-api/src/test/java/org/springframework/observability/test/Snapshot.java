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

package org.springframework.observability.test;

import org.springframework.observability.time.Clock;

/**
 * @author Jonatan Ivanov
 */
public final class Snapshot implements Clock {

	private final long wallTime;

	private final long monotonicTime;

	public static Snapshot of(Clock clock) {
		return new Snapshot(clock.wallTime(), clock.monotonicTime());
	}

	private Snapshot(long wallTime, long monotonicTime) {
		this.wallTime = wallTime;
		this.monotonicTime = monotonicTime;
	}

	@Override
	public long wallTime() {
		return this.wallTime;
	}

	@Override
	public long monotonicTime() {
		return this.monotonicTime;
	}

}
