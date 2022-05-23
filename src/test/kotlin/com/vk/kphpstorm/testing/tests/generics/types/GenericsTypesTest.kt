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
            "generics/types/classes/implicit/standalone.fixture.php",
            "generics/types/classes/implicit/array.fixture.php",
            "generics/types/classes/implicit/nullable.fixture.php",
            "generics/types/classes/implicit/union.fixture.php",
            "generics/types/classes/implicit/tuple.fixture.php",
            "generics/types/classes/implicit/shape.fixture.php",
            "generics/types/classes/implicit/mixed.fixture.php",
            "generics/.meta/functions.php"
        )
    }

    fun testExplicitClass() {
        runFixture(
            "generics/types/classes/explicit/standalone.fixture.php",
            "generics/types/classes/explicit/array.fixture.php",
            "generics/types/classes/explicit/nullable.fixture.php",
            "generics/types/classes/explicit/union.fixture.php",
            "generics/types/classes/explicit/tuple.fixture.php",
            "generics/types/classes/explicit/shape.fixture.php",
            "generics/types/classes/explicit/mixed.fixture.php",
            "generics/.meta/functions.php"
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
