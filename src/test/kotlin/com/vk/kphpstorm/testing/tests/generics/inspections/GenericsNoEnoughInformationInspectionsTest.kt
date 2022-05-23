package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.GenericNoEnoughInformationInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class GenericsNoEnoughInformationInspectionsTest : InspectionTestBase(GenericNoEnoughInformationInspection()) {
    fun test() {
        runFixture(
            "generics/inspections/no_enough_information.fixture.php",
        )
    }
}
