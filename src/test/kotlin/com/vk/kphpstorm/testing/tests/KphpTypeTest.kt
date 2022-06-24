package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.testing.infrastructure.TypeTestBase

class KphpTypeTest : TypeTestBase() {

    fun testJsonTag() {
        runFixture("kphp_type/json_tag.good.fixture.php")
    }

}
