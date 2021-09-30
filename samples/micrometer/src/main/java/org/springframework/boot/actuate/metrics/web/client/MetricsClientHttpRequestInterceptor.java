/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.actuate.metrics.web.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import io.micrometer.core.instrument.Sample;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.actuate.metrics.AutoTimer;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.core.NamedThreadLocal;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.observability.event.Recorder;
import org.springframework.observability.event.interval.IntervalHttpClientEvent;
import org.springframework.observability.transport.http.HttpClientRequest;
import org.springframework.observability.transport.http.HttpClientResponse;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriTemplateHandler;

/**
 * {@link ClientHttpRequestInterceptor} applied via a
 * {@link MetricsRestTemplateCustomizer} to record metrics.
 *
 * @author Jon Schneider
 * @author Phillip Webb
 */
class MetricsClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final Log logger = LogFactory.getLog(MetricsClientHttpRequestInterceptor.class);

	private static final ThreadLocal<Deque<String>> urlTemplate = new UrlTemplateThreadLocal();

	private final Recorder<?> recorder;

	private final RestTemplateExchangeTagsProvider tagProvider;

	private final String metricName;

	private final AutoTimer autoTimer;

	/**
	 * Create a new {@code MetricsClientHttpRequestInterceptor}.
	 * @param recorder the registry to which metrics are recorded
	 * @param tagProvider provider for metrics tags
	 * @param metricName name of the metric to record
	 * @param autoTimer the auto-timers to apply or {@code null} to disable auto-timing
	 * @since 2.2.0
	 */
	MetricsClientHttpRequestInterceptor(Recorder<?> recorder, RestTemplateExchangeTagsProvider tagProvider,
			String metricName, AutoTimer autoTimer) {
		this.tagProvider = tagProvider;
		this.recorder = recorder;
		this.metricName = metricName;
		this.autoTimer = (autoTimer != null) ? autoTimer : AutoTimer.DISABLED;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		if (!enabled()) {
			return execution.execute(request, body);
		}
		HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
		IntervalHttpClientEvent event = new IntervalHttpClientEvent(requestWrapper) {
			@Override
			public String getLowCardinalityName() {
				return metricName;
			}
		};
		Sample sample = this.autoTimer.builder(this.metricName, this.recorder)
				.description("Timer of RestTemplate operation");
		ClientHttpResponse response = null;
		Throwable error = null;
		try {
			sample.start();
			response = execution.execute(request, body);
			return response;
		}
		catch (Throwable e) {
			error = e;
			sample.error(e);
			throw e;
		}
		finally {
			try {
				event.setResponse(new ClientHttpResponseWrapper(requestWrapper, response, error));
				sample.stop();
			}
			catch (Exception ex) {
				logger.info("Failed to record metrics.", ex);
			}
			if (urlTemplate.get().isEmpty()) {
				urlTemplate.remove();
			}
		}
	}

	private boolean enabled() {
		return this.autoTimer.isEnabled();
	}

	UriTemplateHandler createUriTemplateHandler(UriTemplateHandler delegate) {
		if (delegate instanceof RootUriTemplateHandler) {
			return ((RootUriTemplateHandler) delegate).withHandlerWrapper(CapturingUriTemplateHandler::new);
		}
		return new CapturingUriTemplateHandler(delegate);
	}

	private final class CapturingUriTemplateHandler implements UriTemplateHandler {

		private final UriTemplateHandler delegate;

		private CapturingUriTemplateHandler(UriTemplateHandler delegate) {
			this.delegate = delegate;
		}

		@Override
		public URI expand(String url, Map<String, ?> arguments) {
			if (enabled()) {
				urlTemplate.get().push(url);
			}
			return this.delegate.expand(url, arguments);
		}

		@Override
		public URI expand(String url, Object... arguments) {
			if (enabled()) {
				urlTemplate.get().push(url);
			}
			return this.delegate.expand(url, arguments);
		}

	}

	private static final class UrlTemplateThreadLocal extends NamedThreadLocal<Deque<String>> {

		private UrlTemplateThreadLocal() {
			super("Rest Template URL Template");
		}

		@Override
		protected Deque<String> initialValue() {
			return new LinkedList<>();
		}

	}

	private static final class HttpRequestWrapper implements HttpClientRequest {

		final HttpRequest delegate;

		HttpRequestWrapper(HttpRequest delegate) {
			this.delegate = delegate;
		}

		@Override
		public Collection<String> headerNames() {
			return this.delegate.getHeaders().keySet();
		}

		@Override
		public Object unwrap() {
			return this.delegate;
		}

		@Override
		public String method() {
			return this.delegate.getMethod().name();
		}

		@Override
		public String path() {
			return this.delegate.getURI().getPath();
		}

		@Override
		public String url() {
			return this.delegate.getURI().toString();
		}

		@Override
		public String header(String name) {
			Object result = this.delegate.getHeaders().getFirst(name);
			return result != null ? result.toString() : null;
		}

		@Override
		public void header(String name, String value) {
			this.delegate.getHeaders().set(name, value);
		}

	}

	private static final class ClientHttpResponseWrapper implements HttpClientResponse {

		final HttpRequestWrapper request;

		@Nullable
		final ClientHttpResponse response;

		@Nullable
		final Throwable error;

		ClientHttpResponseWrapper(HttpRequestWrapper request, @Nullable ClientHttpResponse response,
				@Nullable Throwable error) {
			this.request = request;
			this.response = response;
			this.error = error;
		}

		@Override
		public Object unwrap() {
			return this.response;
		}

		@Override
		public Collection<String> headerNames() {
			return this.response != null ? this.response.getHeaders().keySet() : Collections.emptyList();
		}

		@Override
		public HttpClientRequest request() {
			return this.request;
		}

		@Override
		public Throwable error() {
			return this.error;
		}

		@Override
		public int statusCode() {
			try {
				int result = this.response != null ? this.response.getRawStatusCode() : 0;
				if (result <= 0 && this.error instanceof HttpStatusCodeException) {
					result = ((HttpStatusCodeException) this.error).getRawStatusCode();
				}
				return result;
			}
			catch (Exception e) {
				return 0;
			}
		}

	}

}
