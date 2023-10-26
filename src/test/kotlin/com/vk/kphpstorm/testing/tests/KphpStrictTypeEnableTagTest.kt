package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpStrictTypesEnableInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpStrictTypeEnableTagTest : InspectionTestBase(KphpStrictTypesEnableInspection()) {
    fun `test strict type with tag`() {
        runFixture("strict_typing/declare_strict_type_enable.fixture.php")
    }

    fun `test strict type without tag`() {
        runFixture("strict_typing/declare_strict_type_enable_miss.fixture.php")
    }
}