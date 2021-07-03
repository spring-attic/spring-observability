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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.observability.event.tag.Cardinality.HIGH;
import static org.springframework.observability.event.tag.Cardinality.LOW;

/**
 * @author Jonatan Ivanov
 */
class ImmutableTagTest {

	@Test
	void nullKeyIsNotAllowed() {
		assertThatThrownBy(() -> new ImmutableTag(null, "", LOW)).isExactlyInstanceOf(NullPointerException.class)
				.hasMessage("key can't be null").hasNoCause();
	}

	@Test
	void nullValueIsNotAllowed() {
		assertThatThrownBy(() -> new ImmutableTag("", null, HIGH)).isExactlyInstanceOf(NullPointerException.class)
				.hasMessage("value can't be null").hasNoCause();
	}

	@Test
	void nullCardinalityIsNotAllowed() {
		assertThatThrownBy(() -> new ImmutableTag("", "", null)).isExactlyInstanceOf(NullPointerException.class)
				.hasMessage("cardinality can't be null").hasNoCause();
	}

	@Test
	void gettersShouldReturnTheRightValues() {
		String key = "testKey";
		String value = "testValue";

		Tag tag = new ImmutableTag(key, value, LOW);
		assertThat(tag.getKey()).isSameAs(key);
		assertThat(tag.getValue()).isSameAs(value);
		assertThat(tag.getCardinality()).isSameAs(LOW);
	}

	@Test
	void equalsAndHashCodeShouldWork() {
		Tag tag = new ImmutableTag("testKey", "testValue", LOW);
		assertThat(tag).isEqualTo(tag);
		assertThat(tag).isNotEqualTo(null);
		assertThat(tag).isNotEqualTo("tag");
		assertThat(tag).isNotEqualTo(new ImmutableTag("", "testValue", LOW));
		assertThat(tag).isNotEqualTo(new ImmutableTag("testKey", "", LOW));
		assertThat(tag).isEqualTo(new ImmutableTag("testKey", "testValue", LOW));
		assertThat(tag).isEqualTo(new ImmutableTag("testKey", "testValue", HIGH));

		assertThat(tag).hasSameHashCodeAs(tag);
		assertThat(tag).doesNotHaveSameHashCodeAs("tag");
		assertThat(tag).doesNotHaveSameHashCodeAs(new ImmutableTag("", "testValue", LOW));
		assertThat(tag).doesNotHaveSameHashCodeAs(new ImmutableTag("testKey", "", LOW));
		assertThat(tag).hasSameHashCodeAs(new ImmutableTag("testKey", "testValue", LOW));
		assertThat(tag).hasSameHashCodeAs(new ImmutableTag("testKey", "testValue", HIGH));
	}

	@Test
	void toStringShouldWork() {
		assertThat(new ImmutableTag("testKey", "testValue", LOW)).hasToString("tag{testKey=testValue}");
	}

}
