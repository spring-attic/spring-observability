/*
 * Copyright 2013-2021 the original author or authors.
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

package org.springframework.observability.tracing;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import static org.assertj.core.api.BDDAssertions.then;

class ThreadLocalSpanTests {

	@Test
	void should_set_and_retrieve_values_from_thread_local() {
		// given
		ThreadLocalSpan span = new ThreadLocalSpan(new WithMockSpanTracer());
		Span firstSpan = BDDMockito.mock(Span.class);
		Span secondSpan = BDDMockito.mock(Span.class);

		// when
		span.set(firstSpan);

		// then
		SpanAndScope spanAndScope = span.get();
		then(spanAndScope).isNotNull();
		then(spanAndScope.getSpan()).isSameAs(firstSpan);

		// when
		span.set(secondSpan);

		// then
		spanAndScope = span.get();
		then(spanAndScope).isNotNull();
		then(spanAndScope.getSpan()).isSameAs(secondSpan);

		// when
		span.remove();

		// then
		spanAndScope = span.get();
		then(spanAndScope).isNotNull();
		then(spanAndScope.getSpan()).isSameAs(firstSpan);

		// when
		span.remove();
		spanAndScope = span.get();
		then(spanAndScope).isNull();
	}

	@Test
	void should_create_a_new_thread_local_span() {
		// given
		ThreadLocalSpan span = new ThreadLocalSpan(new WithMockSpanTracer());

		// when
		Span firstSpan = span.nextSpan();

		// then
		then(span.get().getSpan()).isSameAs(firstSpan);
	}

}
