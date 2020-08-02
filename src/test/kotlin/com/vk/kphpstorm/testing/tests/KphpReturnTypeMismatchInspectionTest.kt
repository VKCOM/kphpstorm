package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpReturnTypeMismatchInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpReturnTypeMismatchInspectionTest : InspectionTestBase(KphpReturnTypeMismatchInspection()) {

    fun testReturnStatement1() {
        runFixture("strict_typing/return_statement_1.fixture.php")
    }

    fun testReturnStatement2() {
        runFixture("strict_typing/return_statement_2.fixture.php")
    }

    fun testReturnStatement3() {
        runFixture("strict_typing/return_statement_3.fixture.php")
    }

    fun testReturnStatement4() {
        runFixture("strict_typing/return_statement_4.fixture.php")
    }

    fun testReturnStatement5() {
        runFixture("strict_typing/return_statement_5.fixture.php")
    }

    fun testReturnStatement6() {
        runFixture("strict_typing/return_statement_6.fixture.php")
    }

    fun testReturnStatement7() {
        runFixture("strict_typing/return_statement_7.fixture.php")
    }

    fun testReturnStatement8() {
        runFixture("strict_typing/return_statement_8.fixture.php")
    }

    fun testReturnStatement9() {
        runFixture("strict_typing/return_statement_9.fixture.php")
    }

    fun testReturnMismatch() {
        runFixture("strict_typing/return_mismatch.fixture.php")
    }

}
