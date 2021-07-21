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
public interface Tag {

	/**
	 * @return The key of the tag, it mustn't be null.
	 */
	String getKey();

	/**
	 * @return The value of the tag, it mustn't be null.
	 */
	String getValue();

	/**
	 * @return The cardinality of the tag, it mustn't be null.
	 */
	Cardinality getCardinality();

	/**
	 * @param key The key of the tag, it mustn't be null.
	 * @param value The value of the tag, it mustn't be null.
	 * @param cardinality The cardinality of the tag, it mustn't be null.
	 * @return An immutable Tag instance with the given parameters.
	 */
	static Tag of(String key, String value, Cardinality cardinality) {
		return new ImmutableTag(key, value, cardinality);
	}

}
