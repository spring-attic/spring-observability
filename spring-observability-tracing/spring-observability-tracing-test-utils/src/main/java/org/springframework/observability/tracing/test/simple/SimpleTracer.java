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

package org.springframework.observability.tracing.test.simple;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.observability.tracing.BaggageInScope;
import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.ScopedSpan;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.SpanCustomizer;
import org.springframework.observability.tracing.TraceContext;
import org.springframework.observability.tracing.Tracer;

/**
 * A test tracer implementation. Puts started span in a list.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class SimpleTracer implements Tracer {

	/**
	 * Recorded spans.
	 */
	public Deque<SimpleSpan> spans = new LinkedList<>();

	@Override
	public Span nextSpan(Span parent) {
		return new SimpleSpan();
	}

	/**
	 * @return a single reported span
	 */
	public SimpleSpan getOnlySpan() {
		assertTrue(this.spans.size() == 1, "There must be only one span");
		SimpleSpan span = this.spans.getFirst();
		assertTrue(span.started, "Span must be started");
		assertTrue(span.ended, "Span must be finished");
		return span;
	}

	private void assertTrue(boolean condition, String text) {
		if (!condition) {
			throw new AssertionError(text);
		}
	}

	/**
	 * @return the last reported span
	 */
	public SimpleSpan getLastSpan() {
		assertTrue(!this.spans.isEmpty(), "There must be at least one span");
		SimpleSpan span = this.spans.getLast();
		assertTrue(span.started, "Span must be started");
		return span;
	}

	@Override
	public SpanInScope withSpan(Span span) {
		return new SimpleSpanInScope();
	}

	@Override
	public SpanCustomizer currentSpanCustomizer() {
		return null;
	}

	@Override
	public Span currentSpan() {
		if (this.spans.isEmpty()) {
			return null;
		}
		return this.spans.getLast();
	}

	@Override
	public SimpleSpan nextSpan() {
		final SimpleSpan span = new SimpleSpan();
		this.spans.add(span);
		return span;
	}

	@Override
	public ScopedSpan startScopedSpan(String name) {
		return null;
	}

	@Override
	public Span.Builder spanBuilder() {
		return new SimpleSpanBuilder(this);
	}

	@Override
	public TraceContext.Builder traceContextBuilder() {
		return null;
	}

	@Override
	public CurrentTraceContext currentTraceContext() {
		return null;
	}

	@Override
	public Map<String, String> getAllBaggage() {
		return new HashMap<>();
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

}
