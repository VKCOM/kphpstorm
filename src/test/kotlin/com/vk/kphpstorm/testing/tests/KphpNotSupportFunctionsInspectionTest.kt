package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpNotSupportFunctionsInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpNotSupportFunctionsInspectionTest : InspectionTestBase(KphpNotSupportFunctionsInspection()) {

    fun testNotSupportFunctions() {
        runFixture("kphp_functions/not_support_functions.fixture.php")
    }

    fun testNotSupportFunctionsWithNamespace1() {
        runFixture("kphp_functions/not_support_functions_with_namespace-1.fixture.php")
    }

    fun testNotSupportFunctionsWithNamespace2() {
        runFixture("kphp_functions/not_support_functions_with_namespace-2.fixture.php")
    }

    fun testNotSupportFunctionsWithNamespace3() {
        runFixture("kphp_functions/not_support_functions_with_namespace-3.fixture.php")
    }

}
