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

package org.springframework.observability.event;

/**
 * An Event represents that something happened. You must always name your events and
 * should provide a meaningful description if you can.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
public interface Event {

	/**
	 * @return The name of the event, the method mustn't return null. The method must
	 * return values with low cardinality.
	 */
	String getLowCardinalityName();

	/**
	 * @return The description of the event, the method shouldn't return null.
	 */
	default String getDescription() {
		return "";
	}

}
