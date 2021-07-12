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

package org.springframework.observability.test;

import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;
import org.springframework.observability.time.Clock;

/**
 * @author Jonatan Ivanov
 */
public class TestRecordingListener implements RecordingListener<TestContext> {

	private final Clock clock;

	private TestContext context;

	private IntervalRecording<TestContext> onStartRecording;

	private IntervalRecording<TestContext> onStopRecording;

	private IntervalRecording<TestContext> onErrorRecording;

	private InstantRecording instantRecording;

	private Snapshot onStartSnapshot;

	private Snapshot onStopSnapshot;

	private Snapshot onErrorSnapshot;

	public TestRecordingListener(Clock clock) {
		this.clock = clock;
	}

	@Override
	public TestContext createContext() {
		this.context = new TestContext();
		return this.context;
	}

	@Override
	public void onStart(IntervalRecording<TestContext> intervalRecording) {
		this.onStartSnapshot = Snapshot.of(this.clock);
		this.onStartRecording = intervalRecording;
	}

	@Override
	public void onStop(IntervalRecording<TestContext> intervalRecording) {
		this.onStopSnapshot = Snapshot.of(this.clock);
		this.onStopRecording = intervalRecording;
	}

	@Override
	public void onError(IntervalRecording<TestContext> intervalRecording) {
		this.onErrorSnapshot = Snapshot.of(this.clock);
		this.onErrorRecording = intervalRecording;
	}

	@Override
	public void record(InstantRecording instantRecording) {
		this.instantRecording = instantRecording;
	}

	public IntervalRecording<TestContext> getOnStartRecording() {
		return this.onStartRecording;
	}

	public IntervalRecording<TestContext> getOnStopRecording() {
		return this.onStopRecording;
	}

	public IntervalRecording<TestContext> getOnErrorRecording() {
		return this.onErrorRecording;
	}

	public InstantRecording getInstantRecording() {
		return instantRecording;
	}

	public TestContext getContext() {
		return this.context;
	}

	public Snapshot getOnStartSnapshot() {
		return this.onStartSnapshot;
	}

	public Snapshot getOnStopSnapshot() {
		return this.onStopSnapshot;
	}

	public Snapshot getOnErrorSnapshot() {
		return this.onErrorSnapshot;
	}

	public void reset() {
		this.context = null;
		this.onStartRecording = null;
		this.onStopRecording = null;
		this.onErrorRecording = null;
		this.instantRecording = null;
		this.onStartSnapshot = null;
		this.onStopSnapshot = null;
		this.onErrorSnapshot = null;
	}

}
