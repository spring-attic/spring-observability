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

import org.springframework.observability.event.interval.IntervalEvent;

/**
 * @author Jonatan Ivanov
 */
public enum TestIntervalEvent implements IntervalEvent {

	INTERVAL_EVENT("test-interval-event", "Test event to be able to record interval events");

	private final String name;

	private final String description;

	TestIntervalEvent(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getLowCardinalityName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

}
