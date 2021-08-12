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

import java.util.List;

import org.springframework.observability.event.listener.RecordingListener;

/**
 * Using this {@link RecordingListener} implementation, you can register multiple
 * listeners.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface CompositeRecordingListener extends RecordingListener<CompositeContext> {

	/**
	 * @return registered listeners
	 */
	List<? extends RecordingListener<?>> getListeners();

}
