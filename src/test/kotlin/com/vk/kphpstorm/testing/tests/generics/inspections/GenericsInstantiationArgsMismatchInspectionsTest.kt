package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.GenericInstantiationArgsCountMismatchInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class GenericsInstantiationArgsMismatchInspectionsTest : InspectionTestBase(GenericInstantiationArgsCountMismatchInspection()) {
    fun test() {
        runFixture(
            "generics/inspections/instantiation_args_mismatch.fixture.php",
        )
    }
}
