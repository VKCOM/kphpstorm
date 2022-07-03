package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpUnsupportedFunctionCallInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpUnsupportedFunctionCallInspectionTest : InspectionTestBase(KphpUnsupportedFunctionCallInspection()) {

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
