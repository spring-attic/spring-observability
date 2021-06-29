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

import java.util.List;

import org.springframework.observability.tracing.Span;
import org.springframework.observability.tracing.exporter.FinishedSpan;

/**
 * A testing span handler that takes hold of finished spans.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public interface TestSpanHandler extends Iterable<FinishedSpan> {

	/**
	 * @return a list of {@link FinishedSpan}s
	 */
	List<FinishedSpan> reportedSpans();

	/**
	 * @return a first local finished span
	 */
	FinishedSpan takeLocalSpan();

	/**
	 * Clears the reported finished spans.
	 */
	void clear();

	/**
	 * @param kind kind of a span to take
	 * @return picks the first remote span of a given kind
	 */
	FinishedSpan takeRemoteSpan(Span.Kind kind);

	/**
	 * @param kind kind of a span to take
	 * @return picks the first remote span of a given kind with an error
	 */
	FinishedSpan takeRemoteSpanWithError(Span.Kind kind);

	/**
	 * @param index index of the finished span
	 * @return a {@link FinishedSpan} with a given index
	 */
	FinishedSpan get(int index);

}
