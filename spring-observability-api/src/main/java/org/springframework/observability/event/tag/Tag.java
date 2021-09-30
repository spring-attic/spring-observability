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

package org.springframework.observability.event.tag;

import org.springframework.observability.event.Recording;

/**
 * Represents a key-vale pair in order to attach data to a {@link Recording}.
 * Implementations must make sure that none of the methods return null.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface Tag extends Comparable<Tag> {

	/**
	 * Tag key.
	 * @return tag key
	 */
	String getKey();

	/**
	 * Tag value.
	 * @return tag value
	 */
	String getValue();

	/**
	 * Tag cardinality.
	 * @return tag cardinality
	 */
	default Cardinality getCardinality() {
		return Cardinality.LOW;
	}

	/**
	 * Builds a tag.
	 * @param key tag key
	 * @param value tag value
	 * @return tag
	 */
	static Tag of(String key, String value) {
		return Tag.of(key, value, Cardinality.LOW); // TODO: or HIGH?
	}

	/**
	 * Builds a tag.
	 * @param key tag key
	 * @param value tag value
	 * @param cardinality tag cardinality
	 * @return tag
	 */
	static Tag of(String key, String value, Cardinality cardinality) {
		return new ImmutableTag(key, value, cardinality);
	}

	@Override
	default int compareTo(Tag o) {
		return getKey().compareTo(o.getKey());
	}

}
