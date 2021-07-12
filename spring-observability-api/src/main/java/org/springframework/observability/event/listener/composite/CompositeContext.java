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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.observability.event.listener.RecordingListener;

/**
 * Context holder in case of using listeners that have different context types. It
 * basically holds a listener -> context mapping so that you can query context of the
 * listener by passing a listener instance.
 *
 * @author Jonatan Ivanov
 * @see CompositeRecordingListener
 */
public class CompositeContext {

	private final Map<RecordingListener<?>, Object> contexts = new HashMap<>();

	CompositeContext(RecordingListener<?>... listeners) {
		this(Arrays.asList(listeners));
	}

	CompositeContext(List<RecordingListener<?>> listeners) {
		// Could be a .stream().collect(toMap(...)) but toMap fails on null values:
		// https://bugs.openjdk.java.net/browse/JDK-8148463
		for (RecordingListener<?> listener : listeners) {
			this.contexts.put(listener, listener.createContext());
		}
	}

	@SuppressWarnings("unchecked")
	<T> T byListener(RecordingListener<T> listener) {
		return (T) this.contexts.get(listener);
	}

}
