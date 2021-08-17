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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalLongRunningHttpServerEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.tag.Cardinality;

/**
 * {@link RecordingListener} that uses Micrometer's API to record long running tasks.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class MicrometerLongRunningTaskRecordingListener
		implements MetricsRecordingListener<MicrometerLongRunningTaskRecordingListener.LongRunningTaskContext> {

	private final MeterRegistry registry;

	/**
	 * @param registry The registry to use to record events.
	 */
	public MicrometerLongRunningTaskRecordingListener(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public boolean isApplicable(Recording<?, ?> recording) {
		return recording.getEvent() instanceof IntervalLongRunningHttpServerEvent;
	}

	@Override
	public void onStart(IntervalRecording<LongRunningTaskContext> intervalRecording) {
		LongTaskTimer.Sample sample = LongTaskTimer.builder(intervalRecording.getEvent().getLowCardinalityName())
				.description(intervalRecording.getEvent().getDescription()).tags(toTags(intervalRecording))
				.tags(toTags(intervalRecording)).register(this.registry).start();
		intervalRecording.getContext().addSample(sample);
	}

	@Override
	public void onStop(IntervalRecording<LongRunningTaskContext> intervalRecording) {
		intervalRecording.getContext().getSamples().forEach(LongTaskTimer.Sample::stop);
	}

	@Override
	public void onError(IntervalRecording<LongRunningTaskContext> intervalRecording) {
		// TODO: If error add a tag
	}

	@Override
	public void record(InstantRecording instantRecording) {

	}

	@Override
	public LongRunningTaskContext createContext() {
		return new LongRunningTaskContext();
	}

	private List<Tag> toTags(Recording<?, ?> recording) {
		return StreamSupport.stream(recording.getTags().spliterator(), false)
				.filter(tag -> tag.getCardinality() == Cardinality.LOW).map(tag -> Tag.of(tag.getKey(), tag.getValue()))
				.collect(Collectors.toList());
	}

	static class LongRunningTaskContext {

		private final List<LongTaskTimer.Sample> samples = new ArrayList<>();

		void addSample(LongTaskTimer.Sample sample) {
			this.samples.add(sample);
		}

		List<LongTaskTimer.Sample> getSamples() {
			return this.samples;
		}

	}

}
