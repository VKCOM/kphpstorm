package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.KphpGenericsInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpGenericsInspectionsTest : InspectionTestBase(KphpGenericsInspection()) {
    fun testInspections() {
        runFixture(
            "generics/inspections/bound_violation.fixture.php",
            "generics/inspections/no_enough_information.fixture.php",
            "generics/inspections/instantiation_args_mismatch.fixture.php",
        )
    }

    fun testReifyFromReturn() {
        runFixture("generics/types/extended-reify/return.fixture.php",)
        runFixture("generics/types/extended-reify/return_wrong.fixture.php")
        runFixture("generics/types/extended-reify/param.fixture.php")
        runFixture("generics/types/extended-reify/param_wrong.fixture.php")
    }
}
