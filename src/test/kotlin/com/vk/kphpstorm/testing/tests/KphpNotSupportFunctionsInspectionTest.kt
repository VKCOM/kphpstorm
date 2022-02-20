package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpNotSupportFunctionsInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpNotSupportFunctionsInspectionTest : InspectionTestBase(KphpNotSupportFunctionsInspection()) {

    fun testNotSupportFunctions() {
        runFixture("kphp_functions/not_support_functions.fixture.php")
    }

}
