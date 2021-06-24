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

package org.springframework.observability.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;

/**
 * Represents a {@link Span} stored in thread local.
 *
 * @author Marcin Grzejszczak
 * @since 3.1.0
 */
public interface WithThreadLocalSpan {

	/**
	 * Logger.
	 */
	Logger log = LoggerFactory.getLogger(WithThreadLocalSpan.class);

	/**
	 * Sets the span in thread local scope.
	 * @param span span to put in thread local
	 */
	default void setSpanInScope(Span span) {
		getThreadLocalSpan().set(span);
		log.debug("Put span in scope {}", span);
	}

	/**
	 * Finishes the thread local span.
	 * @param error potential error to be stored in span
	 */
	default void finishSpan(@Nullable Throwable error) {
		SpanAndScope spanAndScope = takeSpanFromThreadLocal();
		if (spanAndScope == null) {
			return;
		}
		Span span = spanAndScope.getSpan();
		Tracer.SpanInScope scope = spanAndScope.getScope();
		if (span.isNoop()) {
			log.debug("Span {} is noop - will stop the scope", span);
			scope.close();
			return;
		}
		if (error != null) { // an error occurred, adding error to span
			span.error(error);
		}
		log.debug("Will finish the span and its corresponding scope {}", span);
		span.end();
		scope.close();
	}

	/**
	 * Takes a span from thread local and restores the previous one if present.
	 * @return span from a thread local span
	 */
	default SpanAndScope takeSpanFromThreadLocal() {
		SpanAndScope span = getThreadLocalSpan().get();
		log.debug("Took span [{}] from thread local", span);
		getThreadLocalSpan().remove();
		return span;
	}

	/**
	 * @return thread local span
	 */
	ThreadLocalSpan getThreadLocalSpan();

}
