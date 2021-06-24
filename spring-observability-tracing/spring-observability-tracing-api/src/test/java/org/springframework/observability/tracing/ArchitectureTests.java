/*
 * Copyright 2013-2021 the original author or authors.
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

package org.springframework.observability.tracing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.springframework.core.annotation.AliasFor;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = ArchitectureTests.class, importOptions = ArchitectureTests.ProductionCode.class)
public class ArchitectureTests {

	private static final List<Class<?>> ALLOWED_SPRING_FRAMEWORK_DEPENDENCIES = Arrays.asList(Nullable.class,
			StringUtils.class, AliasFor.class);

	@ArchTest
	public static final ArchRule should_not_contain_any_spring_reference_in_module_other_than_the_allowed_ones = noClasses()
			.should()
			.dependOnClassesThat(new DescribedPredicate<>("You may only depend on "
					+ ALLOWED_SPRING_FRAMEWORK_DEPENDENCIES.stream().map(Class::getName).collect(Collectors.toList())
					+ " classes from Spring Framework dependency") {
				@Override
				public boolean apply(JavaClass javaClass) {
					JavaPackage aPackage = javaClass.getPackage();
					String packageName = aPackage.getName();
					if (!packageName.startsWith("org.springframework")
							|| packageName.startsWith("org.springframework.observability")) {
						return false;
					}
					return ALLOWED_SPRING_FRAMEWORK_DEPENDENCIES.stream()
							.noneMatch(aClass -> aClass.getName().equals(javaClass.getFullName()));
				}
			});

	@ArchTest
	public static final ArchRule should_not_introduce_package_cycles = slices()
			.matching("org.springframework.observability.(*)..").should().beFreeOfCycles();

	static class ProductionCode implements ImportOption {

		@Override
		public boolean includes(Location location) {
			return Predefined.DO_NOT_INCLUDE_TESTS.includes(location)
					&& Predefined.DO_NOT_INCLUDE_JARS.includes(location);
		}

	}

}
