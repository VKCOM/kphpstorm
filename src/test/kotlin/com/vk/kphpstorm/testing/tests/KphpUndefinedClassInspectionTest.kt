package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpUndefinedClassInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpUndefinedClassInspectionTest : InspectionTestBase(KphpUndefinedClassInspection()) {

    fun testKphpUndefinedClassPhpdocs() {
        runFixture("strict_typing/phpdocs_with_kphp_types.fixture.php")
    }

    fun testKphpUndefinedClassImport() {
        runFixture("strict_typing/undef_class_import.fixture.php")
    }

}
