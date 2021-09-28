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
import org.springframework.observability.event.listener.composite.CompositeContext;
import org.springframework.observability.time.Clock;

/**
 * @author Jonatan Ivanov
 */
public class TestRecordingListener implements RecordingListener<CompositeContext> {

	private final Clock clock;

	private CompositeContext context;

	private IntervalRecording onStartRecording;

	private IntervalRecording onCreateRecording;

	private IntervalRecording onStopRecording;

	private IntervalRecording onErrorRecording;

	private IntervalRecording onRestoreRecording;

	private InstantRecording instantRecording;

	private Snapshot onCreateSnapshot;

	private Snapshot onStartSnapshot;

	private Snapshot onStopSnapshot;

	private Snapshot onErrorSnapshot;

	private Snapshot onRestoreSnapshot;

	public TestRecordingListener(Clock clock) {
		this.clock = clock;
	}

	@Override
	public CompositeContext createContext() {
		this.context = new CompositeContext();
		return this.context;
	}

	@Override
	public void onCreate(IntervalRecording intervalRecording) {
		this.onCreateSnapshot = Snapshot.of(this.clock);
		this.onCreateRecording = intervalRecording;
	}

	@Override
	public void onStart(IntervalRecording intervalRecording) {
		this.onStartSnapshot = Snapshot.of(this.clock);
		this.onStartRecording = intervalRecording;
	}

	@Override
	public void onStop(IntervalRecording intervalRecording) {
		this.onStopSnapshot = Snapshot.of(this.clock);
		this.onStopRecording = intervalRecording;
	}

	@Override
	public void onError(IntervalRecording intervalRecording) {
		this.onErrorSnapshot = Snapshot.of(this.clock);
		this.onErrorRecording = intervalRecording;
	}

	@Override
	public void onRestore(IntervalRecording intervalRecording) {
		this.onRestoreSnapshot = Snapshot.of(this.clock);
		this.onRestoreRecording = intervalRecording;
	}

	@Override
	public void recordInstant(InstantRecording instantRecording) {
		this.instantRecording = instantRecording;
	}

	public IntervalRecording getOnStartRecording() {
		return this.onStartRecording;
	}

	public IntervalRecording getOnStopRecording() {
		return this.onStopRecording;
	}

	public IntervalRecording getOnErrorRecording() {
		return this.onErrorRecording;
	}

	public InstantRecording getInstantRecording() {
		return instantRecording;
	}

	public CompositeContext getContext() {
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
