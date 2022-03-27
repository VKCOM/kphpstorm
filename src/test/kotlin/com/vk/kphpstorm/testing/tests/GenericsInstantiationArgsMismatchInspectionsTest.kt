package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.GenericInstantiationArgsCountMismatchInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class GenericsInstantiationArgsMismatchInspectionsTest : InspectionTestBase(GenericInstantiationArgsCountMismatchInspection()) {
    fun testClasses() {
        runFixture(
            "generics/classes/instantiation_args_mismatch.inspection.fixture.php",
            "generics/classes/Vector.fixture.php"
        )
    }
}
