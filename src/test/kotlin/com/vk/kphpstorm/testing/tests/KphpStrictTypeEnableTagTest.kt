package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpStrictTypesEnableInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpStrictTypeEnableTagTest : InspectionTestBase(KphpStrictTypesEnableInspection()) {
    fun `test strict type with tag`() {
        runFixture("kphpdoc_inspection/kphp_strict_type_enable/declare_strict_type_enable.good.fixture.php")
    }

    fun `test declare another argument`(){
        runFixture("kphpdoc_inspection/kphp_strict_type_enable/declare_another_argument.good.fixture.php")
    }

    fun `test strict type another value`(){
        runFixture("kphpdoc_inspection/kphp_strict_type_enable/declare_strict_type_another_value.good.fixture.php")
    }

    fun `test strict type without tag`() {
        runFixture("kphpdoc_inspection/kphp_strict_type_enable/declare_strict_type_enable_miss.fixture.php")
    }

    fun `test strict type tag not near above declare`() {
        runFixture("kphpdoc_inspection/kphp_strict_type_enable/declare_strict_type_enable_not_near.fixture.php")
    }
}
