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

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class SpanAndScopeTests {

	@Test
	void should_close_span_and_scope() {
		Span span = mock(Span.class);
		Tracer.SpanInScope scope = mock(Tracer.SpanInScope.class);
		SpanAndScope spanAndScope = new SpanAndScope(span, scope);

		spanAndScope.close();

		then(span).should().end();
		then(scope).should().close();
	}

}
