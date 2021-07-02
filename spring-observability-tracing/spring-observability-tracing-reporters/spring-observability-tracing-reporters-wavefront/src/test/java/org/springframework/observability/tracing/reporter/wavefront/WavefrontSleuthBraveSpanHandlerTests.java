/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.observability.tracing.reporter.wavefront;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import org.springframework.observability.tracing.exporter.FinishedSpan;

class WavefrontSleuthBraveSpanHandlerTests {

	@Test
	void should_delegate_to_generic_sleuth_span_handler() {
		WavefrontSleuthSpanHandler sleuthSpanHandler = BDDMockito.mock(WavefrontSleuthSpanHandler.class);
		WavefrontSleuthBraveSpanHandler handler = new WavefrontSleuthBraveSpanHandler(sleuthSpanHandler);

		handler.end(TraceContext.newBuilder().traceId(1L).spanId(2L).build(), new MutableSpan(),
				SpanHandler.Cause.FINISHED);

		BDDMockito.then(sleuthSpanHandler).should().end(
				BDDMockito.any(org.springframework.observability.tracing.TraceContext.class),
				BDDMockito.any(FinishedSpan.class));
	}

}
