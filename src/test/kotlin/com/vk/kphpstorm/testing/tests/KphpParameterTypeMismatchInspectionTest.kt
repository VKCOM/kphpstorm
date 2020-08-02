package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpParameterTypeMismatchInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpParameterTypeMismatchInspectionTest : InspectionTestBase(KphpParameterTypeMismatchInspection()) {

    fun testCallArgs1() {
        runFixture("strict_typing/call_args_1.fixture.php")
    }

    fun testCallArgs2() {
        runFixture("strict_typing/call_args_2.fixture.php")
    }

    fun testCallArgs3() {
        runFixture("strict_typing/call_args_3.fixture.php")
    }

    fun testCallArgs4() {
        runFixture("strict_typing/call_args_4.fixture.php")
    }

    fun testCallArgs5() {
        runFixture("strict_typing/call_args_5.fixture.php")
    }

    fun testCallArgs6() {
        runFixture("strict_typing/call_args_6.fixture.php")
    }

    fun testCallArgs7() {
        runFixture("strict_typing/call_args_7.fixture.php")
    }

    fun testCallArgs8() {
        runFixture("strict_typing/call_args_8.fixture.php")
    }

    fun testCallArgs9() {
        runFixture("strict_typing/call_args_9.fixture.php")
    }

}
