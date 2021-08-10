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
import java.util.Collections;

import org.springframework.observability.event.tag.Tag;

/**
 * No-op implementation of {@link IntervalRecording} that does nothing. This is useful in
 * case recording is turned off.
 *
 * @param <T> Context Type
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class NoOpIntervalRecording<T> implements IntervalRecording<T> {

	private static final IntervalEvent EVENT = new NoOpIntervalEvent();

	private static final String HIGH_CARDINALITY_NAME = EVENT.getLowCardinalityName();

	private static final Iterable<Tag> TAGS = Collections.emptyList();

	@Override
	public IntervalEvent getEvent() {
		return EVENT;
	}

	@Override
	public String getHighCardinalityName() {
		return HIGH_CARDINALITY_NAME;
	}

	@Override
	public IntervalRecording<T> highCardinalityName(String highCardinalityName) {
		return this;
	}

	@Override
	public Iterable<Tag> getTags() {
		return TAGS;
	}

	@Override
	public IntervalRecording<T> tag(Tag tag) {
		return this;
	}

	@Override
	public Duration getDuration() {
		return Duration.ZERO;
	}

	@Override
	public long getStartNanos() {
		return 0;
	}

	@Override
	public long getStopNanos() {
		return 0;
	}

	@Override
	public long getStartWallTime() {
		return 0;
	}

	@Override
	public IntervalRecording<T> start() {
		return this;
	}

	@Override
	public IntervalRecording<T> start(long wallTime, long monotonicTime) {
		return this;
	}

	@Override
	public void stop() {
	}

	@Override
	public void stop(long monotonicTime) {
	}

	@Override
	public Throwable getError() {
		return null;
	}

	@Override
	public IntervalRecording<T> error(Throwable error) {
		return this;
	}

	@Override
	public T getContext() {
		return null;
	}

	@Override
	public String toString() {
		return "NoOpIntervalRecording";
	}

	static class NoOpIntervalEvent implements IntervalEvent {

		@Override
		public String getLowCardinalityName() {
			return "noop";
		}

		@Override
		public String getDescription() {
			return "noop";
		}

	}

}
