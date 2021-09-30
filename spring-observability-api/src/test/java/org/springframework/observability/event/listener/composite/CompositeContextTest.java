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

package org.springframework.observability.event.listener.composite;

import org.junit.jupiter.api.Test;

import org.springframework.observability.event.listener.RecordingListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonatan Ivanov
 */
class CompositeContextTest {

	@Test
	void shouldGiveTheRightContextToTheRightListener() {
		RecordingListener<CompositeContext> listener1 = mock(RecordingListener.class);
		CompositeContext context1 = new CompositeContext();
		when(listener1.createContext()).thenReturn(context1);

		RecordingListener<String> listener2 = mock(RecordingListener.class);
		String context2 = "context2";
		when(listener2.createContext()).thenReturn(context2);

		RecordingListener<Void> listener3 = mock(RecordingListener.class);
		Void context3 = null;
		when(listener3.createContext()).thenReturn(null);

		CompositeContext context = new CompositeContext(listener1, listener2, listener3);
		assertThat(context.byListener(listener1)).isSameAs(context1);
		assertThat(context.byListener(listener2)).isSameAs(context2);
		assertThat(context.byListener(listener3)).isSameAs(context3);
	}

}
