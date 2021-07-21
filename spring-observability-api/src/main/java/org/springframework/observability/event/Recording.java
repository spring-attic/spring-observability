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

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.tag.Tag;

/**
 * A Recording represents an observation of an {@link Event}. It can give you details
 * about the occurrence of an {@link Event}, see the implementations for details.
 *
 * Implementations must make sure that none of the methods return null.
 *
 * @param <E> Event Type
 * @param <R> Recording Type
 * @author Jonatan Ivanov
 * @since 1.0.0
 * @see InstantRecording
 * @see IntervalRecording
 */
public interface Recording<E extends Event, R extends Recording<E, R>> {

	/**
	 * @return The {@link Event} this recording belongs to.
	 */
	E getEvent();

	/**
	 * @return The {@link Tag Tags} added to this recording.
	 */
	Iterable<Tag> getTags();

	/**
	 * @param tag {@link Tag} to be added to the recording.
	 * @return itself.
	 */
	R tag(Tag tag);

}
