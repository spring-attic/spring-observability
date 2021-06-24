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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class SamplerFunctionTests {

	@Test
	void should_defer_decision() {
		then(SamplerFunction.deferDecision()).isSameAs(SamplerFunction.Constants.DEFER_DECISION);
	}

	@Test
	void should_never_sample() {
		then(SamplerFunction.neverSample()).isSameAs(SamplerFunction.Constants.NEVER_SAMPLE);
	}

	@Test
	void should_always_decision() {
		then(SamplerFunction.alwaysSample()).isSameAs(SamplerFunction.Constants.ALWAYS_SAMPLE);
	}

}
