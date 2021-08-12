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
import java.util.List;

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;

/**
 * Using this {@link RecordingListener} implementation, you can register multiple
 * listeners and handled them as one; method calls will be delegated to each registered
 * listener.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class AllMatchingRecordingListener implements CompositeRecordingListener {

	private final List<RecordingListener<?>> listeners;

	/**
	 * @param listeners The listeners that are registered under the composite.
	 */
	public AllMatchingRecordingListener(RecordingListener<?>... listeners) {
		this(Arrays.asList(listeners));
	}

	/**
	 * @param listeners The listeners that are registered under the composite.
	 */
	public AllMatchingRecordingListener(List<RecordingListener<?>> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void onStart(IntervalRecording<CompositeContext> intervalRecording) {
		this.listeners.stream().filter(listener -> listener.isApplicable(intervalRecording))
				.forEach(listener -> listener.onStart(new IntervalRecordingView<>(listener, intervalRecording)));
	}

	@Override
	public void onStop(IntervalRecording<CompositeContext> intervalRecording) {
		this.listeners.stream().filter(listener -> listener.isApplicable(intervalRecording))
				.forEach(listener -> listener.onStop(new IntervalRecordingView<>(listener, intervalRecording)));
	}

	@Override
	public void onError(IntervalRecording<CompositeContext> intervalRecording) {
		this.listeners.stream().filter(listener -> listener.isApplicable(intervalRecording))
				.forEach(listener -> listener.onError(new IntervalRecordingView<>(listener, intervalRecording)));
	}

	@Override
	public void record(InstantRecording instantRecording) {
		this.listeners.stream().filter(listener -> listener.isApplicable(instantRecording))
				.forEach(listener -> listener.record(instantRecording));
	}

	@Override
	public CompositeContext createContext() {
		return new CompositeContext(this.listeners);
	}

	@Override
	public List<RecordingListener<?>> getListeners() {
		return this.listeners;
	}

}
