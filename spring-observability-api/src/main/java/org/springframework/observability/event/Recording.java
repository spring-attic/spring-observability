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
import org.springframework.observability.event.tag.Tags;

/**
 * A Recording represents an observation of an {@link Event}. It can give you details
 * about the occurrence of an {@link Event}, see the implementations for details.
 *
 * Implementations must make sure that none of the methods return null.
 *
 * @param <E> event type
 * @param <R> recording Type
 * @author Jonatan Ivanov
 * @since 1.0.0
 * @see InstantRecording
 * @see IntervalRecording
 */
public interface Recording<E extends Event, R extends Recording<E, R>> {

	/**
	 * The {@link Event} this recording belongs to.
	 * @return the event
	 */
	E getEvent();

	/**
	 * The high-cardinality (detailed) name of the recording. Unlike
	 * {@link Event#getLowCardinalityName()}, this method can return high-cardinality
	 * values and can be overwritten on-the-fly.
	 * @return the high-cardinality (detailed) name of the recording
	 */
	String getHighCardinalityName();

	/**
	 * Sets the high cardinality name.
	 * @param highCardinalityName the new high-cardinality (detailed) name of the
	 * recording
	 * @return this
	 */
	R highCardinalityName(String highCardinalityName);

	/**
	 * The {@link Tag Tags} added to this recording.
	 * @return the {@link Tag Tags}
	 */
	Iterable<Tag> getTags();

	/**
	 * Adds the {@link Tag} to the recording.
	 * @param tag {@link Tag} to be added to the recording
	 * @return this
	 */
	R tag(Tag tag);

	/**
	 * Adds the {@link Tag} to the recording with the given key and value.
	 * @param key tag key
	 * @param value tag value
	 * @return this
	 */
	default R tag(String key, String value) {
		return tag(Tag.of(key, value));
	}

	/**
	 * Adds the {@link Tag} to the recording with the given key and value pairs.
	 * @param tags array of tags
	 * @return this
	 */
	default R tags(String... tags) {
		return tags(Tags.of(tags));
	}

	/**
	 * Adds the {@link Tag}s to the recording.
	 * @param tags {@link Tag}s to be added to the recording
	 * @return this
	 */
	default R tags(Iterable<Tag> tags) {
		R result = null;
		for (Tag tag : tags) {
			result = tag(tag);
		}
		return result;
	}

}
