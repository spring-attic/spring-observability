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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.TraceContext;

/**
 * A test span builder implementation.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class SimpleSpanBuilder implements Span.Builder {

	/**
	 * List of events.
	 */
	public List<String> events = new ArrayList<>();

	/**
	 * Map of tags.
	 */
	public Map<String, String> tags = new HashMap<>();

	/**
	 * Throwable corresponding to the span.
	 */
	public Throwable throwable;

	/**
	 * Remote service name of the span.
	 */
	public String remoteServiceName;

	/**
	 * Span kind.
	 */
	public Span.Kind spanKind;

	/**
	 * Span name.
	 */
	public String name;

	/**
	 * Remote service ip.
	 */
	public String ip;

	/**
	 * Remote service port.
	 */
	public int port;

	/**
	 * Simple tracer.
	 */
	public SimpleTracer simpleTracer;

	/**
	 * @param simpleTracer simple tracer
	 */
	public SimpleSpanBuilder(SimpleTracer simpleTracer) {
		this.simpleTracer = simpleTracer;
	}

	@Override
	public Span.Builder setParent(TraceContext context) {
		return this;
	}

	@Override
	public Span.Builder setNoParent() {
		return this;
	}

	@Override
	public Span.Builder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public Span.Builder event(String value) {
		this.events.add(value);
		return this;
	}

	@Override
	public Span.Builder tag(String key, String value) {
		this.tags.put(key, value);
		return this;
	}

	@Override
	public Span.Builder error(Throwable throwable) {
		this.throwable = throwable;
		return this;
	}

	@Override
	public Span.Builder kind(Span.Kind spanKind) {
		this.spanKind = spanKind;
		return this;
	}

	@Override
	public Span.Builder remoteServiceName(String remoteServiceName) {
		this.remoteServiceName = remoteServiceName;
		return this;
	}

	@Override
	public Span.Builder remoteIpAndPort(String ip, int port) {
		this.ip = ip;
		this.port = port;
		return this;
	}

	@Override
	public Span start() {
		SimpleSpan span = new SimpleSpan();
		this.tags.forEach(span::tag);
		this.events.forEach(span::event);
		span.remoteServiceName(this.remoteServiceName);
		span.error(this.throwable);
		span.spanKind = this.spanKind;
		span.name(this.name);
		span.remoteIpAndPort(this.ip, this.port);
		span.start();
		simpleTracer.spans.add(span);
		return span;
	}

	@Override
	public Span start(long micros) {
		Span start = start();
		return start.start(micros);
	}

}
