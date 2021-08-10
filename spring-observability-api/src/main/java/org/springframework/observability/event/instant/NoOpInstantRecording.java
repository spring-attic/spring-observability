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

import java.util.Collections;

import org.springframework.observability.event.tag.Tag;

/**
 * No-op implementation of {@link InstantRecording} that does nothing. This is useful in
 * case recording is turned off.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class NoOpInstantRecording implements InstantRecording {

	private static final InstantEvent EVENT = new NoOpInstantEvent();

	private static final String HIGH_CARDINALITY_NAME = EVENT.getLowCardinalityName();

	private static final Iterable<Tag> TAGS = Collections.emptyList();

	@Override
	public InstantEvent getEvent() {
		return EVENT;
	}

	@Override
	public String getHighCardinalityName() {
		return HIGH_CARDINALITY_NAME;
	}

	@Override
	public InstantRecording highCardinalityName(String highCardinalityName) {
		return this;
	}

	@Override
	public Iterable<Tag> getTags() {
		return TAGS;
	}

	@Override
	public InstantRecording tag(Tag tag) {
		return this;
	}

	@Override
	public void record() {
	}

	@Override
	public void record(long nanos) {

	}

	@Override
	public Long eventNanos() {
		return 0L;
	}

	@Override
	public String toString() {
		return "NoOpInstantRecording";
	}

	static class NoOpInstantEvent implements InstantEvent {

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
