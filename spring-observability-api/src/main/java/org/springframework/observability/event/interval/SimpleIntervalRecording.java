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

package org.springframework.observability.event.interval;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.event.tag.Tag;
import org.springframework.observability.time.Clock;

/**
 * @param <T> Context Type
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public class SimpleIntervalRecording<T> implements IntervalRecording<T> {

	private final IntervalEvent event;

	private String detailedName;

	private final RecordingListener<T> listener;

	private final T context;

	private final Clock clock;

	private Duration duration = Duration.ZERO;

	private long started = 0;

	private long stopped = 0;

	private long startWallTime = 0;

	private final Set<Tag> tags = new LinkedHashSet<>();

	private Throwable error = null;

	/**
	 * @param event The event this recording belongs to.
	 * @param listener The listener that needs to be notified about the recordings.
	 * @param clock The clock to be used.
	 */
	public SimpleIntervalRecording(IntervalEvent event, RecordingListener<T> listener, Clock clock) {
		this.event = event;
		this.detailedName = event.getName();
		this.listener = listener;
		this.context = listener.createContext();
		this.clock = clock;
	}

	@Override
	public IntervalEvent getEvent() {
		return this.event;
	}

	@Override
	public String getDetailedName() {
		return this.detailedName;
	}

	@Override
	public IntervalRecording<T> detailedName(String detailedName) {
		this.detailedName = detailedName;
		return this;
	}

	@Override
	public Duration getDuration() {
		return this.duration;
	}

	@Override
	public long getStartNanos() {
		return this.started;
	}

	@Override
	public IntervalRecording<T> start() {
		if (this.started != 0) {
			throw new IllegalStateException("IntervalRecording has already been started");
		}

		this.startWallTime = clock.wallTime();
		this.started = clock.monotonicTime();
		this.listener.onStart(this);

		return this;
	}

	@Override
	public long getStopNanos() {
		return this.stopped;
	}

	@Override
	public long getStartWallTime() {
		return this.startWallTime;
	}

	@Override
	public void stop() {
		verifyIfHasStarted();
		verifyIfHasNotStopped();
		this.stopped = clock.monotonicTime();
		this.duration = Duration.ofNanos(this.stopped - this.started);
		this.listener.onStop(this);
	}

	@Override
	public Iterable<Tag> getTags() {
		return Collections.unmodifiableSet(this.tags);
	}

	@Override
	public IntervalRecording<T> tag(Tag tag) {
		verifyIfHasNotStopped();
		this.tags.add(tag);
		return this;
	}

	@Override
	public Throwable getError() {
		return this.error;
	}

	@Override
	public IntervalRecording<T> error(Throwable error) {
		verifyIfHasStarted();
		verifyIfHasNotStopped();
		if (this.error != null) {
			throw new IllegalStateException("Only one error can be attached");
		}

		this.error = error;
		this.listener.onError(this);
		return this;
	}

	@Override
	public T getContext() {
		return this.context;
	}

	@Override
	public String toString() {
		return "{" + "event=" + event.getName() + ", detailedName=" + detailedName + ", duration=" + duration.toMillis()
				+ "ms" + ", tags=" + tags + ", error=" + error + '}';
	}

	private void verifyIfHasStarted() {
		if (this.started == 0) {
			throw new IllegalStateException("IntervalRecording hasn't been started");
		}
	}

	private void verifyIfHasNotStopped() {
		if (this.stopped != 0) {
			throw new IllegalStateException("IntervalRecording has already been stopped");
		}
	}

}
