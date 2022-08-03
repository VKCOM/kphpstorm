package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.testing.infrastructure.TypeTestBase

class KphpTypeTest : TypeTestBase() {

    fun testFfiTag() {
        runFixture("kphp_type/ffi_tag.good.fixture.php")
    }

    fun testJsonTag() {
        runFixture("kphp_type/json_tag.good.fixture.php")
    }

    fun testArrayForce() {
        runFixture("kphp_type/json_tag.good.fixture.php")
    }

}
