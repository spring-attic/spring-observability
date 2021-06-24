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

import java.util.Map;

import org.mockito.BDDMockito;

class WithMockSpanTracer implements Tracer {

	SpanInScope lastSpanInScope;

	@Override
	public Map<String, String> getAllBaggage() {
		return null;
	}

	@Override
	public BaggageInScope getBaggage(String name) {
		return null;
	}

	@Override
	public BaggageInScope getBaggage(TraceContext traceContext, String name) {
		return null;
	}

	@Override
	public BaggageInScope createBaggage(String name) {
		return null;
	}

	@Override
	public BaggageInScope createBaggage(String name, String value) {
		return null;
	}

	@Override
	public Span nextSpan() {
		return null;
	}

	@Override
	public Span nextSpan(Span parent) {
		return null;
	}

	@Override
	public SpanInScope withSpan(Span span) {
		this.lastSpanInScope = BDDMockito.mock(Tracer.SpanInScope.class);
		return this.lastSpanInScope;
	}

	@Override
	public ScopedSpan startScopedSpan(String name) {
		return null;
	}

	@Override
	public Span.Builder spanBuilder() {
		return null;
	}

	@Override
	public TraceContext.Builder traceContextBuilder() {
		return null;
	}

	@Override
	public SpanCustomizer currentSpanCustomizer() {
		return null;
	}

	@Override
	public Span currentSpan() {
		return null;
	}

}
