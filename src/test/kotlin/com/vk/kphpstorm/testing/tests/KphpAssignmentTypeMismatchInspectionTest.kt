package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpAssignmentTypeMismatchInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpAssignmentTypeMismatchInspectionTest : InspectionTestBase(KphpAssignmentTypeMismatchInspection()) {

    fun testPropsAssignments1() {
        runFixture("strict_typing/props_assignments_1.fixture.php")
    }

    fun testPropsAssignments2() {
        runFixture("strict_typing/props_assignments_2.fixture.php")
    }

    fun testPropsAssignments3() {
        runFixture("strict_typing/props_assignments_3.fixture.php")
    }

    fun testPropsAssignments4() {
        runFixture("strict_typing/props_assignments_4.fixture.php")
    }

    fun testPropsAssignments5() {
        runFixture("strict_typing/props_assignments_5.fixture.php")
    }

    fun testPropsAssignments6() {
        runFixture("strict_typing/props_assignments_6.fixture.php")
    }

    fun testPropsAssignments7() {
        runFixture("strict_typing/props_assignments_7.fixture.php")
    }

    fun testPropsAssignments8() {
        runFixture("strict_typing/props_assignments_8.fixture.php")
    }

    fun testPropsAssignments9() {
        runFixture("strict_typing/props_assignments_9.fixture.php")
    }

    fun testPropsAssignments10() {
        runFixture("strict_typing/props_assignments_10.fixture.php")
    }

    fun testPropsAssignments11() {
        runFixture("strict_typing/props_assignments_11.fixture.php")
    }

    fun testPropsAssignments12() {
        runFixture("strict_typing/props_assignments_12.fixture.php")
    }

}
