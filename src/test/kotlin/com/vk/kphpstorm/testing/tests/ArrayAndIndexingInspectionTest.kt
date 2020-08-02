package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.ArrayAndIndexingInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class ArrayAndIndexingInspectionTest : InspectionTestBase(ArrayAndIndexingInspection()) {

    fun testForeachArgument() {
        runFixture("strict_typing/foreach_argument.fixture.php")
    }

    fun testIndexingExpression() {
        runFixture("strict_typing/indexing_expression.fixture.php")
    }

}
