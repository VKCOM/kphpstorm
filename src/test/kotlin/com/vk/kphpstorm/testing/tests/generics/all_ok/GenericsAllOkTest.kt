package com.vk.kphpstorm.testing.tests.generics.all_ok

import com.vk.kphpstorm.testing.infrastructure.AllOkTestBase

class GenericsAllOkTest : AllOkTestBase() {
    fun testNewExpr() {
        runFixture("generics/all_ok/new_expr.fixture.php")
    }

    fun testVectorUse() {
        runFixture("generics/Containers/Vector.php")
        runFixture("generics/all_ok/vector_use.fixture.php")
    }
}
