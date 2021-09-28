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

import org.springframework.observability.lang.NonNull;
import org.springframework.observability.lang.Nullable;
import org.springframework.observability.transport.http.Request;
import org.springframework.observability.transport.http.Response;

/**
 * An IntervalEvent that represents an HTTP event.
 *
 * @param <REQ> request type
 * @param <RES> response type
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public interface IntervalHttpEvent<REQ extends Request, RES extends Response> extends IntervalEvent {

	/**
	 * Returns the HTTP request.
	 * @return request
	 */
	@NonNull
	REQ getRequest();

	/**
	 * Returns the HTTP response.
	 * @return response
	 */
	@Nullable
	RES getResponse();

	/**
	 * Sets the given HTTP response on the event. Might be {@code null} when an exception
	 * occurred and there's no response.
	 * @param response a HTTP response
	 * @return this
	 */
	IntervalHttpEvent<REQ, RES> setResponse(RES response);

}
