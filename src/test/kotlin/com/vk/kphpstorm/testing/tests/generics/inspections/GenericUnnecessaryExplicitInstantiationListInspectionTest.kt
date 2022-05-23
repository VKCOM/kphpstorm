package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.GenericUnnecessaryExplicitInstantiationListInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class GenericUnnecessaryExplicitInstantiationListInspectionTest
    : InspectionTestBase(GenericUnnecessaryExplicitInstantiationListInspection()) {

    fun test() {
        runFixture(
            "generics/inspections/unnecessary_explicit_instantiation_list.fixture.php",
        )
    }
}
