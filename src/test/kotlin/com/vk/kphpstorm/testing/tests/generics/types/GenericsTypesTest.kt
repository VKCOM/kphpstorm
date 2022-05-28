package com.vk.kphpstorm.testing.tests.generics.types

import com.vk.kphpstorm.testing.infrastructure.TypeTestBase

class GenericsTypesTest : TypeTestBase() {
    fun testExplicitPrimitives() {
        runFixture(
            "generics/types/primitives/explicit/simple.fixture.php",
            "generics/types/primitives/explicit/union.fixture.php",
            "generics/.meta/functions.php"
        )
    }

    fun testImplicitPrimitives() {
        runFixture(
            "generics/types/primitives/implicit/simple.fixture.php",
            "generics/types/primitives/implicit/union.fixture.php",
            "generics/.meta/functions.php"
        )
    }

    fun testClassStrings() {
        runFixture(
            "generics/types/class-string/explicit.fixture.php",
            "generics/types/class-string/implicit.fixture.php",
            "generics/.meta/functions.php"
        )
    }

    fun testImplicitClass() {
        runFixture(
            "generics/types/classes-as-types/implicit/standalone.fixture.php",
            "generics/types/classes-as-types/implicit/array.fixture.php",
            "generics/types/classes-as-types/implicit/nullable.fixture.php",
            "generics/types/classes-as-types/implicit/union.fixture.php",
            "generics/types/classes-as-types/implicit/tuple.fixture.php",
            "generics/types/classes-as-types/implicit/shape.fixture.php",
            "generics/types/classes-as-types/implicit/mixed.fixture.php",
            "generics/.meta/functions.php",
        )
    }

    fun testExplicitClass() {
        runFixture(
            "generics/types/classes-as-types/explicit/standalone.fixture.php",
            "generics/types/classes-as-types/explicit/array.fixture.php",
            "generics/types/classes-as-types/explicit/nullable.fixture.php",
            "generics/types/classes-as-types/explicit/union.fixture.php",
            "generics/types/classes-as-types/explicit/tuple.fixture.php",
            "generics/types/classes-as-types/explicit/shape.fixture.php",
            "generics/types/classes-as-types/explicit/mixed.fixture.php",
            "generics/.meta/functions.php"
        )
    }

    fun testNexExpr() {
        runFixture(
            "generics/types/classes/new_expr.fixture.php",
        )
    }

    fun testFunctions() {
        runFixture(
            "generics/types/functions/chain.fixture.php",
            "generics/Containers/Vector.php",
        )
    }

    fun testMethods() {
        runFixture(
            "generics/types/methods/static_and_non_generic.fixture.php",
            "generics/types/methods/chain.fixture.php",
            "generics/types/methods/complex.fixture.php",
            "generics/Containers/Vector.php",
            "generics/Containers/Pair.php",
        )
    }

    fun testDefaultTypes() {
        runFixture(
            "generics/types/extended-reify/default_type.fixture.php",
            "generics/Containers/Vector.php",
            "generics/Containers/Pair.php",
        )
    }

    fun testReifier() {
        runFixture(
            "generics/types/reify/main.fixture.php",
        )
    }

    // TODO:
//    fun testSimpleFunctions() {
//        runFixture("generics/simple_functions.fixture.php")
//    }

    // Classes
    // TODO
//    fun testSimpleClasses() {
//        runFixture("generics/classes/simple_classes.fixture.php", "generics/classes/Vector.fixture.php")
//    }
}
