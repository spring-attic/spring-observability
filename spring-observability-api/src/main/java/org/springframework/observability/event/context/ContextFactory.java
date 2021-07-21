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

package org.springframework.observability.event.context;

/**
 * Factory interface to create arbitrary context objects. Implementations must make sure
 * that the factory can't return null.
 *
 * @author Jonatan Ivanov
 * @since 1.0.0
 */
@FunctionalInterface
public interface ContextFactory<T> {

	/**
	 * @return A new context instance.
	 */
	T createContext();

}
