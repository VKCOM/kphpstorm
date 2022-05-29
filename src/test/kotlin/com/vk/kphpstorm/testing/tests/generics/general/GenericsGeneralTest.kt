package com.vk.kphpstorm.testing.tests.generics.general

import com.vk.kphpstorm.testing.infrastructure.GenericTestBase

class GenericsGeneralTest : GenericTestBase() {
    fun testNewExpr() {
        runFixture("generics/general/new_expr.fixture.php")
    }

    fun testVectorUse() {
        runFixture("generics/Containers/Vector.php")
        runFixture("generics/general/vector_use.fixture.php")
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
        runFixture("generics/general/reifier/default/wrong.fixture.php")
    }

    fun testReifyFromReturn() {
        runFixture("generics/general/reifier/context/return.fixture.php",)
        runFixture("generics/general/reifier/context/return_wrong.fixture.php")
    }

    fun testReifyFromParam() {
        runFixture("generics/general/reifier/context/param.fixture.php")
        runFixture("generics/general/reifier/context/param_wrong.fixture.php")
    }
}
