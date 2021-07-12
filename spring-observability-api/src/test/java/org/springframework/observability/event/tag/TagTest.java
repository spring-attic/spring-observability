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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.observability.event.tag.Cardinality.HIGH;

/**
 * @author Jonatan Ivanov
 */
class TagTest {

	@Test
	void shouldCreateAnImmutableTag() {
		String key = "testKey";
		String value = "testValue";

		Tag tag = new ImmutableTag(key, value, HIGH);
		assertThat(tag).isExactlyInstanceOf(ImmutableTag.class);
		assertThat(tag.getKey()).isSameAs(key);
		assertThat(tag.getValue()).isSameAs(value);
		assertThat(tag.getCardinality()).isSameAs(HIGH);
	}

}
