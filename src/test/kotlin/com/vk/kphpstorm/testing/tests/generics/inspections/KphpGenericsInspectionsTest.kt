package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.KphpGenericsInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpGenericsInspectionsTest : InspectionTestBase(KphpGenericsInspection()) {
    fun testInspections() {
        runFixture("generics/inspections/bound_violation.fixture.php")
        runFixture("generics/inspections/no_enough_information.fixture.php")
        runFixture("generics/inspections/instantiation_args_mismatch.fixture.php")
        runFixture("generics/inspections/several_reified_types.fixture.php")
        runFixture("generics/inspections/duplicate_types.fixture.php")
    }

    fun testExtendsTypes() {
        runFixture("generics/inspections/extends/ok.fixture.php")
        runFixture("generics/inspections/extends/wrong.fixture.php")
    }
}
