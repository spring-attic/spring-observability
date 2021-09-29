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

package org.springframework.observability.tracing.test.brave.bridge;

import brave.Tracing;
import brave.handler.MutableSpan;

import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.TraceContext;
import org.springframework.observability.tracing.Tracer;
import org.springframework.observability.tracing.brave.bridge.BraveBaggageManager;
import org.springframework.observability.tracing.brave.bridge.BraveCurrentTraceContext;
import org.springframework.observability.tracing.brave.bridge.BraveFinishedSpan;
import org.springframework.observability.tracing.brave.bridge.BraveHttpClientHandler;
import org.springframework.observability.tracing.brave.bridge.BraveHttpRequestParser;
import org.springframework.observability.tracing.brave.bridge.BraveHttpServerHandler;
import org.springframework.observability.tracing.brave.bridge.BravePropagator;
import org.springframework.observability.tracing.brave.bridge.BraveSpan;
import org.springframework.observability.tracing.brave.bridge.BraveTraceContext;
import org.springframework.observability.tracing.brave.bridge.BraveTracer;
import org.springframework.observability.tracing.exporter.FinishedSpan;
import org.springframework.observability.tracing.http.HttpClientHandler;
import org.springframework.observability.tracing.http.HttpRequestParser;
import org.springframework.observability.tracing.http.HttpServerHandler;
import org.springframework.observability.tracing.propagation.Propagator;

/**
 * A utility class to do conversions to and from Brave.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public final class BraveAccessor {

	private BraveAccessor() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	/**
	 * @param braveTracer Brave delegate
	 * @return Spring Observability version
	 */
	public static Tracer tracer(brave.Tracer braveTracer, brave.propagation.CurrentTraceContext context) {
		return new BraveTracer(braveTracer, context, new BraveBaggageManager());
	}

	/**
	 * @param context Brave delegate
	 * @return Spring Observability version
	 */
	public static CurrentTraceContext currentTraceContext(brave.propagation.CurrentTraceContext context) {
		return BraveCurrentTraceContext.fromBrave(context);
	}

	/**
	 * @param traceContext Brave delegate
	 * @return Spring Observability version
	 */
	public static TraceContext traceContext(brave.propagation.TraceContext traceContext) {
		return BraveTraceContext.fromBrave(traceContext);
	}

	/**
	 * @param traceContext Spring Observability delegate
	 * @return Brave version
	 */
	public static brave.propagation.TraceContext traceContext(TraceContext traceContext) {
		return BraveTraceContext.toBrave(traceContext);
	}

	/**
	 * @param span Spring Observability delegate
	 * @return Brave version
	 */
	public static brave.Span braveSpan(Span span) {
		return BraveSpan.toBrave(span);
	}

	/**
	 * @param tracing Brave delegate
	 * @return Spring Observability version
	 */
	public static Propagator propagator(Tracing tracing) {
		return new BravePropagator(tracing);
	}

	/**
	 * @param delegate Brave delegate
	 * @return Spring Observability version
	 */
	public static HttpClientHandler httpClientHandler(
			brave.http.HttpClientHandler<brave.http.HttpClientRequest, brave.http.HttpClientResponse> delegate) {
		return new BraveHttpClientHandler(delegate);
	}

	/**
	 * @param delegate Brave delegate
	 * @return Spring Observability version
	 */
	public static HttpServerHandler httpServerHandler(
			brave.http.HttpServerHandler<brave.http.HttpServerRequest, brave.http.HttpServerResponse> delegate) {
		return new BraveHttpServerHandler(delegate);
	}

	/**
	 * @param delegate Spring Observability delegate
	 * @return Brave version
	 */
	public static brave.http.HttpRequestParser httpRequestParser(HttpRequestParser delegate) {
		return BraveHttpRequestParser.toBrave(delegate);
	}

	/**
	 * @param mutableSpan Brave delegate
	 * @return Spring Observability version
	 */
	public static FinishedSpan finishedSpan(MutableSpan mutableSpan) {
		return new BraveFinishedSpan(mutableSpan);
	}

}
