package com.vk.kphpstorm.testing.tests.generics.general

import com.vk.kphpstorm.testing.infrastructure.GenericTestBase

class GenericsGeneralTest : GenericTestBase() {
    fun testNewExpr() {
        runFixture("generics/general/new_expr.fixture.php")
    }

    fun testFields() {
        runFixture("generics/general/fields/main.fixture.php")
        runFixture("generics/general/fields/static.fixture.php")
    }

    fun testCallback() {
        runFixture("generics/general/callback/return.fixture.php")
        runFixture("generics/general/callback/param.fixture.php")
    }

    fun testVectorUse() {
        runFixture(
            "generics/general/vector_use.fixture.php",
            "generics/Containers/Vector.php",
            "generics/Containers/Pair.php",
        )
    }

    fun testReifier() {
        runFixture("generics/general/reifier/reify.fixture.php")
    }

    fun testExtendsTypeReifier() {
        runFixture("generics/general/reifier/extends/classes_union.fixture.php")
        runFixture("generics/general/reifier/extends/primitives_union.fixture.php")
        runFixture("generics/general/reifier/extends/callable.fixture.php")
        runFixture("generics/general/reifier/extends/wrong.fixture.php")
    }

    fun testDefaultTypeReifier() {
        runFixture("generics/general/reifier/default/main.fixture.php")
        runFixture(
            "generics/general/reifier/default/wrong.fixture.php",
            "generics/Containers/Pair.php",
        )
        runFixture(
            "generics/general/reifier/default/generic.fixture.php",
            "generics/Containers/Vector.php",
            "generics/Containers/Pair.php",
        )
    }

    fun testReifyFromReturn() {
        runFixture(
            "generics/general/reifier/context/return.fixture.php",
            "generics/general/reifier/context/classes.php",
        )
        runFixture(
            "generics/general/reifier/context/return_wrong.fixture.php",
            "generics/general/reifier/context/classes.php",
        )
    }

    fun testReifyFromParam() {
        runFixture(
            "generics/general/reifier/context/param.fixture.php",
            "generics/general/reifier/context/classes.php"
        )
        runFixture(
            "generics/general/reifier/context/param_wrong.fixture.php",
            "generics/general/reifier/context/classes.php"
        )
    }

    fun testGenericInGeneric() {
        runFixture("generics/general/generic_in_generic.fixture.php")
    }
}
