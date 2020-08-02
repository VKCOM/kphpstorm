package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpUndefinedClassInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpUndefinedClassInspectionTest : InspectionTestBase(KphpUndefinedClassInspection()) {

    fun testKphpUndefinedClassPhpdocs() {
        runFixture("strict_typing/phpdocs_with_kphp_types.fixture.php")
    }

}
