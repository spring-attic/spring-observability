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

import org.junit.jupiter.api.Test;

import org.springframework.observability.event.Recording;
import org.springframework.observability.event.instant.InstantRecording;
import org.springframework.observability.event.interval.IntervalRecording;
import org.springframework.observability.event.listener.RecordingListener;

import static org.assertj.core.api.BDDAssertions.then;

class FirstMatchingRecordingListenerTests {

	MatchingListener matchingListener = new MatchingListener();

	@Test
	void should_run_on_start_only_for_first_matching_listener() {
		FirstMatchingRecordingListener firstMatchingRecordingListener = new FirstMatchingRecordingListener(
				new NotMatchingListener(), this.matchingListener, new NotMatchingListener());

		firstMatchingRecordingListener.onStart(null);

		then(this.matchingListener.started).isTrue();
	}

	@Test
	void should_run_on_stop_only_for_first_matching_listener() {
		FirstMatchingRecordingListener firstMatchingRecordingListener = new FirstMatchingRecordingListener(
				new NotMatchingListener(), this.matchingListener, new NotMatchingListener());

		firstMatchingRecordingListener.onStop(null);

		then(this.matchingListener.stopped).isTrue();
	}

	@Test
	void should_run_on_error_only_for_first_matching_listener() {
		FirstMatchingRecordingListener firstMatchingRecordingListener = new FirstMatchingRecordingListener(
				new NotMatchingListener(), this.matchingListener, new NotMatchingListener());

		firstMatchingRecordingListener.onError(null);

		then(this.matchingListener.errored).isTrue();
	}

	@Test
	void should_run_record_only_for_first_matching_listener() {
		FirstMatchingRecordingListener firstMatchingRecordingListener = new FirstMatchingRecordingListener(
				new NotMatchingListener(), this.matchingListener, new NotMatchingListener());

		firstMatchingRecordingListener.record(null);

		then(this.matchingListener.recorded).isTrue();
	}

	static class MatchingListener implements RecordingListener {

		boolean started;

		boolean stopped;

		boolean errored;

		boolean recorded;

		@Override
		public Object createContext() {
			return null;
		}

		@Override
		public void onStart(IntervalRecording intervalRecording) {
			this.started = true;
		}

		@Override
		public void onStop(IntervalRecording intervalRecording) {
			this.stopped = true;
		}

		@Override
		public void onError(IntervalRecording intervalRecording) {
			this.errored = true;
		}

		@Override
		public void record(InstantRecording instantRecording) {
			this.recorded = true;
		}

	}

	static class NotMatchingListener implements RecordingListener {

		@Override
		public Object createContext() {
			return null;
		}

		@Override
		public void onStart(IntervalRecording intervalRecording) {
			throwAssertionError();
		}

		private void throwAssertionError() {
			throw new AssertionError("Not matching listener must not be called");
		}

		@Override
		public void onStop(IntervalRecording intervalRecording) {
			throwAssertionError();
		}

		@Override
		public void onError(IntervalRecording intervalRecording) {
			throwAssertionError();
		}

		@Override
		public void record(InstantRecording instantRecording) {
			throwAssertionError();
		}

		@Override
		public boolean isApplicable(Recording recording) {
			return false;
		}

	}

}
