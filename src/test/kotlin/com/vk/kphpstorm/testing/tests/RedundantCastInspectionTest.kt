package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.RedundantCastInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class RedundantCastInspectionTest : InspectionTestBase(RedundantCastInspection()) {

    fun testRedundantCast1() {
        runFixture("strict_typing/redundant_cast_1.fixture.php")
    }

}
