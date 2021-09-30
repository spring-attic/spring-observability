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
import java.util.stream.Stream;

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.lang.NonNull;

/**
 * Using this {@link RecordingListener} implementation, you can register multiple
 * listeners and handled them as one; method calls will be delegated to each registered
 * listener.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class AllMatchingCompositeRecordingListener implements CompositeRecordingListener {

	private final List<RecordingListener<?>> listeners;

	/**
	 * Creates a new instance of {@link AllMatchingCompositeRecordingListener}.
	 * @param listeners the listeners that are registered under the composite
	 */
	public AllMatchingCompositeRecordingListener(RecordingListener<?>... listeners) {
		this(Arrays.asList(listeners));
	}

	/**
	 * Creates a new instance of {@link AllMatchingCompositeRecordingListener}.
	 * @param listeners the listeners that are registered under the composite
	 */
	public AllMatchingCompositeRecordingListener(List<RecordingListener<?>> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void onCreate(IntervalRecording intervalRecording) {
		getAllApplicableListeners(intervalRecording).forEach(listener -> listener.onCreate(intervalRecording));
	}

	@Override
	public void onStart(IntervalRecording intervalRecording) {
		getAllApplicableListeners(intervalRecording).forEach(listener -> listener.onStart(intervalRecording));
	}

	@NonNull
	private Stream<RecordingListener<?>> getAllApplicableListeners(IntervalRecording intervalRecording) {
		return this.listeners.stream().filter(listener -> listener.isApplicable(intervalRecording));
	}

	@Override
	public void onStop(IntervalRecording intervalRecording) {
		getAllApplicableListeners(intervalRecording).forEach(listener -> listener.onStop(intervalRecording));
	}

	@Override
	public void onError(IntervalRecording intervalRecording) {
		getAllApplicableListeners(intervalRecording).forEach(listener -> listener.onError(intervalRecording));
	}

	@Override
	public void onRestore(IntervalRecording intervalRecording) {
		getAllApplicableListeners(intervalRecording).forEach(listener -> listener.onRestore(intervalRecording));
	}

	@Override
	public void recordInstant(InstantRecording instantRecording) {
		this.listeners.stream().filter(listener -> listener.isApplicable(instantRecording))
				.forEach(listener -> listener.recordInstant(instantRecording));
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
