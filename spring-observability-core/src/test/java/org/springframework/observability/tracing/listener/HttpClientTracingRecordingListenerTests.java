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

package org.springframework.observability.tracing.listener;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import org.springframework.observability.core.http.HttpClientRequest;
import org.springframework.observability.core.http.HttpClientResponse;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalHttpClientEvent;
import org.springframework.observability.event.interval.SimpleIntervalRecording;
import org.springframework.observability.time.MockClock;
import org.springframework.observability.tracing.test.simple.SimpleCurrentTraceContext;
import org.springframework.observability.tracing.test.simple.SimpleHttpClientHandler;
import org.springframework.observability.tracing.test.simple.SimpleSpan;
import org.springframework.observability.tracing.test.simple.SimpleTracer;

import static org.assertj.core.api.BDDAssertions.then;

class HttpClientTracingRecordingListenerTests {

	SimpleTracer simpleTracer = new SimpleTracer();

	SimpleCurrentTraceContext simpleCurrentTraceContext = SimpleCurrentTraceContext.withTracer(this.simpleTracer);

	SimpleHttpClientHandler simpleHttpClientHandler = new SimpleHttpClientHandler(this.simpleTracer);

	@Test
	void should_be_applicable_for_http_client_events() {
		HttpClientTracingRecordingListener listener = new HttpClientTracingRecordingListener(
				this.simpleCurrentTraceContext, this.simpleHttpClientHandler);

		boolean applicable = listener
				.isApplicable(new SimpleIntervalRecording<>(intervalHttpClientEvent(), listener, new MockClock()));

		then(applicable).isTrue();
	}

	@Test
	void should_be_not_applicable_for_non_http_client_events() {
		HttpClientTracingRecordingListener listener = new HttpClientTracingRecordingListener(
				this.simpleCurrentTraceContext, this.simpleHttpClientHandler);

		boolean applicable = listener.isApplicable(new SimpleIntervalRecording<>(event(), listener, new MockClock()));

		then(applicable).isFalse();
	}

	@Test
	void should_put_span_and_scope_in_context_when_started() {
		HttpClientTracingRecordingListener listener = new HttpClientTracingRecordingListener(
				this.simpleCurrentTraceContext, this.simpleHttpClientHandler);
		SimpleIntervalRecording<HttpClientTracingRecordingListener.TracingContext> recording = new SimpleIntervalRecording<>(
				intervalHttpClientEvent(), listener, new MockClock());

		listener.onStart(recording);

		then(recording.getContext().getSpan()).isSameAs(this.simpleTracer.getLastSpan());
		then(recording.getContext().getScope()).isNotNull();
	}

	@Test
	void should_close_span_and_scope_when_stopped() {
		HttpClientTracingRecordingListener listener = new HttpClientTracingRecordingListener(
				this.simpleCurrentTraceContext, this.simpleHttpClientHandler);
		SimpleIntervalRecording<HttpClientTracingRecordingListener.TracingContext> recording = new SimpleIntervalRecording<>(
				intervalHttpClientEvent(), listener, new MockClock());
		SimpleSpan span = this.simpleTracer.nextSpan().start();
		recording.getContext().setSpan(span);
		recording.getContext().setScope(this.simpleCurrentTraceContext.newScope(span.context()));

		listener.onStop(recording);

		then(this.simpleHttpClientHandler.receiveHandled).as("HTTP client handler must handle received").isTrue();
		then(this.simpleTracer.getOnlySpan()).isSameAs(span);
	}

	private IntervalEvent event() {
		return new IntervalEvent() {
			@Override
			public String getLowCardinalityName() {
				return null;
			}

			@Override
			public String getDescription() {
				return null;
			}
		};
	}

	private IntervalHttpClientEvent intervalHttpClientEvent() {
		return new IntervalHttpClientEvent(null) {
			@Override
			public String getLowCardinalityName() {
				return "name";
			}

			@Override
			public String getDescription() {
				return "description";
			}

			@Override
			public HttpClientRequest getRequest() {
				return BDDMockito.mock(HttpClientRequest.class);
			}

			@Override
			public HttpClientResponse getResponse() {
				return BDDMockito.mock(HttpClientResponse.class);
			}
		};
	}

}
