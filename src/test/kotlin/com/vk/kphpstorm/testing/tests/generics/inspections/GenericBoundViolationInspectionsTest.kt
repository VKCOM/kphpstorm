package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.GenericBoundViolationInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class GenericBoundViolationInspectionsTest : InspectionTestBase(GenericBoundViolationInspection()) {
    fun test() {
        runFixture("generics/inspections/bound_violation.fixture.php")
    }
}
