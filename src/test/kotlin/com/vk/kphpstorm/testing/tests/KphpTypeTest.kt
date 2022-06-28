package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.testing.infrastructure.TypeTestBase

class KphpTypeTest : TypeTestBase() {

    fun testFfiTag() {
        runFixture("kphp_type/ffi_tag.good.fixture.php")
    }

}
