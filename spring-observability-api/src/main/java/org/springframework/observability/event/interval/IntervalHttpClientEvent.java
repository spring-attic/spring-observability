/*
 * Copyright 2021-2021 the original author or authors.
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

package org.springframework.observability.event.interval;

import org.springframework.observability.core.http.HttpClientRequest;
import org.springframework.observability.core.http.HttpClientResponse;
import org.springframework.observability.lang.NonNull;

/**
 * An IntervalEvent that represents an HTTP client event.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public abstract class IntervalHttpClientEvent implements IntervalHttpEvent<HttpClientRequest, HttpClientResponse> {

	private final HttpClientRequest request;

	private HttpClientResponse response;

	/**
	 * @param request http client request
	 */
	public IntervalHttpClientEvent(HttpClientRequest request) {
		this.request = request;
	}

	@NonNull
	@Override
	public HttpClientRequest getRequest() {
		return request;
	}

	@Override
	public IntervalHttpClientEvent setResponse(HttpClientResponse response) {
		this.response = response;
		return this;
	}

	@Override
	public HttpClientResponse getResponse() {
		return this.response;
	}

}
