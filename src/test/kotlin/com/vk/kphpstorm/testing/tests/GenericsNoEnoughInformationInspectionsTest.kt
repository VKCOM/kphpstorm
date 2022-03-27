package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.GenericNoEnoughInformationInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class GenericsNoEnoughInformationInspectionsTest : InspectionTestBase(GenericNoEnoughInformationInspection()) {
    fun testClasses() {
        runFixture(
            "generics/classes/no_enough_information.inspection.fixture.php",
            "generics/classes/Vector.fixture.php"
        )
    }
}
