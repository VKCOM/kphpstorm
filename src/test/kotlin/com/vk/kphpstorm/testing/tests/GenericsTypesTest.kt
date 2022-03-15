package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.testing.infrastructure.TypeTestBase

class GenericsTypesTest : TypeTestBase() {
    fun testSimpleFunctions() {
        runFixture("generics/simple_functions.fixture.php")
    }

    fun testClassResolving() {
        runFixture("generics/class_resolving.fixture.php")
    }
}
