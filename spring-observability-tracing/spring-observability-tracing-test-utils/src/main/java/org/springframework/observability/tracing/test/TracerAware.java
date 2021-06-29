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

package org.springframework.observability.tracing.test;

import org.springframework.observability.tracing.CurrentTraceContext;
import org.springframework.observability.tracing.Tracer;
import org.springframework.observability.tracing.http.HttpClientHandler;
import org.springframework.observability.tracing.http.HttpRequestParser;
import org.springframework.observability.tracing.http.HttpServerHandler;
import org.springframework.observability.tracing.propagation.Propagator;

/**
 * Abstraction that provides all the necessary tracing components.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public interface TracerAware {

	/**
	 * @return a {@link Tracer}
	 */
	Tracer tracer();

	/**
	 * Sets a tracing sampler.
	 * @param sampler tracing sampler
	 * @return this
	 */
	TracerAware sampler(TraceSampler sampler);

	/**
	 * @return a {@link CurrentTraceContext}
	 */
	CurrentTraceContext currentTraceContext();

	/**
	 * @return a {@link Propagator}
	 */
	Propagator propagator();

	/**
	 * @return a {@link HttpServerHandler}
	 */
	HttpServerHandler httpServerHandler();

	/**
	 * Sets a http request parser.
	 * @param httpRequestParser a {@link HttpRequestParser}
	 * @return this
	 */
	TracerAware clientRequestParser(HttpRequestParser httpRequestParser);

	/**
	 * @return a {@link HttpClientHandler}
	 */
	HttpClientHandler httpClientHandler();

	/**
	 * Simple tracing sampler.
	 */
	enum TraceSampler {

		/**
		 * Always sampler.
		 */
		ON,

		/**
		 * Never sampler.
		 */
		OFF

	}

}
