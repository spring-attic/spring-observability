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

package org.springframework.observability.micrometer.listener;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.tag.Cardinality;

/**
 * {@link RecordingListener} that uses Micrometer's API to record events.
 */
public class MicrometerRecordingListener implements MetricsRecordingListener<Void> {

	private final MeterRegistry registry;

	/**
	 * @param registry The registry to use to record events.
	 */
	public MicrometerRecordingListener(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onStart(IntervalRecording<Void> intervalRecording) {
	}

	@Override
	public void onStop(IntervalRecording<Void> intervalRecording) {
		Timer.builder(intervalRecording.getEvent().getLowCardinalityName())
				.description(intervalRecording.getEvent().getDescription()).tags(toTags(intervalRecording))
				.tag("error", intervalRecording.getError() != null
						? intervalRecording.getError().getClass().getSimpleName() : "none")
				.register(registry).record(intervalRecording.getDuration());
	}

	@Override
	public void onError(IntervalRecording<Void> intervalRecording) {
	}

	@Override
	public void record(InstantRecording instantRecording) {
		Counter.builder(instantRecording.getEvent().getLowCardinalityName())
				.description(instantRecording.getEvent().getDescription()).tags(toTags(instantRecording))
				.register(registry).increment();
	}

	@Override
	public Void createContext() {
		return null;
	}

	private List<Tag> toTags(Recording<?, ?> recording) {
		return StreamSupport.stream(recording.getTags().spliterator(), false)
				.filter(tag -> tag.getCardinality() == Cardinality.LOW).map(tag -> Tag.of(tag.getKey(), tag.getValue()))
				.collect(Collectors.toList());
	}

}
