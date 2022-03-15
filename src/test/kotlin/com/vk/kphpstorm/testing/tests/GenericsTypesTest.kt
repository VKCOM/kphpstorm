package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.testing.infrastructure.TypeTestBase

class GenericsTypesTest : TypeTestBase() {
    fun testSimpleFunctions() {
        runFixture("generics/simple_functions.fixture.php")
    }

    fun testClassResolvingExplicit() {
        runFixture("generics/class_resolving.fixture.php")
    }

    fun testClassImplicit() {
        runFixture("generics/class_implicit.fixture.php")
    }

    fun testPrimitivesExplicit() {
        runFixture("generics/primitives_explicit.fixture.php")
    }

    fun testPrimitivesImplicit() {
        runFixture("generics/primitives_implicit.fixture.php")
    }

    fun testClassString() {
        runFixture("generics/class-string.fixture.php")
    }
}
