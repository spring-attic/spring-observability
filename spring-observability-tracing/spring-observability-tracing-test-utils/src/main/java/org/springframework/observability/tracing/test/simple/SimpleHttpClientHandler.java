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

import org.springframework.observability.core.http.HttpClientRequest;
import org.springframework.observability.core.http.HttpClientResponse;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.TraceContext;
import org.springframework.observability.tracing.http.HttpClientHandler;

/**
 * A test http client handler implementation.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class SimpleHttpClientHandler implements HttpClientHandler {

	private final SimpleTracer simpleTracer;

	/**
	 * Was the handle receive method called?
	 */
	public boolean receiveHandled;

	/**
	 * @param simpleTracer simple tracer
	 */
	public SimpleHttpClientHandler(SimpleTracer simpleTracer) {
		this.simpleTracer = simpleTracer;
	}

	@Override
	public Span handleSend(HttpClientRequest request) {
		return this.simpleTracer.nextSpan().start();
	}

	@Override
	public Span handleSend(HttpClientRequest request, TraceContext parent) {
		return this.simpleTracer.nextSpan().start();
	}

	@Override
	public void handleReceive(HttpClientResponse response, Span span) {
		span.end();
		this.receiveHandled = true;
	}

}
