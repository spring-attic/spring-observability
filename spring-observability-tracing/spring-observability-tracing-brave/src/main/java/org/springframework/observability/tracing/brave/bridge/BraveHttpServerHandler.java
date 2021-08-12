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

package org.springframework.observability.tracing.brave.bridge;

import org.springframework.observability.core.http.HttpServerRequest;
import org.springframework.observability.core.http.HttpServerResponse;
import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.http.HttpServerHandler;

/**
 * Brave implementation of a {@link HttpServerHandler}.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class BraveHttpServerHandler implements HttpServerHandler {

	final brave.http.HttpServerHandler<brave.http.HttpServerRequest, brave.http.HttpServerResponse> delegate;

	/**
	 * @param delegate Brave delegate
	 */
	public BraveHttpServerHandler(
			brave.http.HttpServerHandler<brave.http.HttpServerRequest, brave.http.HttpServerResponse> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Span handleReceive(HttpServerRequest request) {
		return BraveSpan.fromBrave(this.delegate.handleReceive(BraveHttpServerRequest.toBrave(request)));
	}

	@Override
	public void handleSend(HttpServerResponse response, Span span) {
		this.delegate.handleSend(BraveHttpServerResponse.toBrave(response), BraveSpan.toBrave(span));
	}

}
