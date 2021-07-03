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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Simple, immutable implementation of the {@link Tag} interface.
 *
 * @author Jonatan Ivanov
 */
public class ImmutableTag implements Tag {

	private final String key;

	private final String value;

	private final Cardinality cardinality;

	/**
	 * @param key The key of the tag, it mustn't be null.
	 * @param value The value of the tag, it mustn't be null.
	 * @param cardinality The cardinality of the tag, it mustn't be null.
	 */
	public ImmutableTag(String key, String value, Cardinality cardinality) {
		this.key = requireNonNull(key);
		this.value = requireNonNull(value);
		this.cardinality = requireNonNull(cardinality);
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public Cardinality getCardinality() {
		return this.cardinality;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ImmutableTag that = (ImmutableTag) o;

		return key.equals(that.key) && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public String toString() {
		return "tag{" + this.key + "=" + this.value + "}";
	}

}
