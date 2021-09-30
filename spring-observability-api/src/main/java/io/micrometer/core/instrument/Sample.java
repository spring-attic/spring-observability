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

package io.micrometer.core.instrument;

import java.time.Duration;
import java.util.Objects;

import org.springframework.observability.event.Recorder;
import org.springframework.observability.event.instant.InstantEvent;
import org.springframework.observability.event.interval.IntervalEvent;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.lang.NonNull;
import org.springframework.observability.lang.Nullable;

/**
 * This class was originally in Timer class in micrometer (Timer.Sample). You would have
 * to call the Timer.start(...) to create a sample and then eventually .stop(Timer timer).
 *
 * What we did is extracted Timer.Sample to a separate class and made it wrap the Recorder
 * API. You have to sets of methods - those that only create the sample and those that
 * create and start them. Both the Sample and the IntervalRecording follow the builder
 * pattern and currently have almost identical API to the one that used to be the
 * `Timer.Builder` or `Timer.Sample`.
 */
public class Sample implements IntervalRecording, AutoCloseable {

	/**
	 * Creates the sample.
	 * @param event interval event
	 * @param recorder recorder
	 * @return created sample
	 */
	public static Sample sample(IntervalEvent event, @NonNull Recorder<?> recorder) {
		Objects.requireNonNull(recorder, "Recorder must not be null");
		return new Sample(recorder, event);
	}

	/**
	 * Creates the sample.
	 * @param lowCardinalityName low cardinality name
	 * @param recorder recorder
	 * @return created sample
	 */
	public static Sample sample(String lowCardinalityName, @NonNull Recorder<?> recorder) {
		Objects.requireNonNull(recorder, "Recorder must not be null");
		return new Sample(recorder, () -> lowCardinalityName);
	}

	/**
	 * Creates the sample with a default event.
	 * @param recorder recorder
	 * @return created sample
	 */
	public static Sample sample(@NonNull Recorder<?> recorder) {
		return sample(() -> "sample", recorder);
	}

	/**
	 * Creates and starts the sample.
	 * @param event event
	 * @param recorder recorder
	 * @return started sample
	 */
	public static Sample start(IntervalEvent event, @NonNull Recorder<?> recorder) {
		return sample(event, recorder).start();
	}

	/**
	 * Creates and starts the sample.
	 * @param lowCardinalityName low cardinality name
	 * @param recorder recorder
	 * @return started sample
	 */
	public static Sample start(String lowCardinalityName, @NonNull Recorder<?> recorder) {
		return sample(() -> lowCardinalityName, recorder).start();
	}

	/**
	 * Creates and starts the sample with the default event.
	 * @param recorder recorder
	 * @return started sample
	 */
	public static Sample start(@NonNull Recorder<?> recorder) {
		return sample(recorder).start();
	}

	private final Recorder<?> recorder;

	private final IntervalRecording recording;

	private String description;

	private String lowCardinalityName;

	Sample(Recorder<?> recorder, IntervalEvent event) {
		this.recorder = recorder;
		this.recording = recorder.recordingFor(event);
	}

	@Override
	public IntervalEvent getEvent() {
		return recording.getEvent();
	}

	@Override
	public String getHighCardinalityName() {
		return recording.getHighCardinalityName();
	}

	@Override
	public Sample highCardinalityName(String highCardinalityName) {
		recording.highCardinalityName(highCardinalityName);
		return this;
	}

	@Override
	public Duration getDuration() {
		return recording.getDuration();
	}

	@Override
	public long getStartNanos() {
		return recording.getStartNanos();
	}

	@Override
	public Sample start() {
		recording.start();
		return this;
	}

	@Override
	public Sample start(long wallTime, long monotonicTime) {
		recording.start(wallTime, monotonicTime);
		return this;
	}

	@Override
	public long getStopNanos() {
		return recording.getStopNanos();
	}

	@Override
	public long getStartWallTime() {
		return recording.getStartWallTime();
	}

	@Override
	public void stop() {
		customizeBeforeStop();
		recording.stop();
	}

	@Override
	public void stop(long monotonicTime) {
		customizeBeforeStop();
		recording.stop(monotonicTime);
	}

	@Override
	public Sample restore() {
		recording.restore();
		return this;
	}

	@Override
	public Iterable<Tag> getTags() {
		return recording.getTags();
	}

	@Override
	public Sample tag(Tag tag) {
		recording.tag(tag);
		return this;
	}

	@Override
	@Nullable
	public Throwable getError() {
		return recording.getError();
	}

	@Override
	public Sample error(Throwable error) {
		recording.error(error);
		return this;
	}

	@Override
	public String toString() {
		return recording.toString();
	}

	@Override
	public void close() {
		customizeBeforeStop();
		recording.close();
	}

	private void customizeBeforeStop() {
		if (this.description != null) {
			getEvent().description(this.description);
		}
		if (this.lowCardinalityName != null) {
			getEvent().lowCardinalityName(this.lowCardinalityName);
		}
		recorder.getRecordingCustomizers().forEach(rc -> rc.customize(recording));
	}

	/**
	 * Sets a description.
	 * @param description a description
	 * @return this
	 */
	public Sample description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Sets a low cardinality name.
	 * @param lowCardinalityName low cardinality name
	 * @return this
	 */
	public Sample lowCardinalityName(String lowCardinalityName) {
		this.lowCardinalityName = lowCardinalityName;
		return this;
	}

	@Override
	public void recordInstant(InstantEvent event) {
		this.recording.recordInstant(event);
	}

	@Override
	@Nullable
	public <T> T getContext(RecordingListener<T> listener) {
		return null;
	}

}
