package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.testing.infrastructure.StubTestBase

class KphpStubTest : StubTestBase() {
    fun testKphpJson() {
        doStubTest("kphp_stub/kphp-json.fixture.php")
    }

    fun testKphpSimpleTags() {
        doStubTest("kphp_stub/kphp-simple-tags.fixture.php")
    }

    fun testKphpTemplate() {
        doStubTest("kphp_stub/kphp-template.fixture.php")
    }

    fun testKphpWarnPerformance() {
        doStubTest("kphp_stub/kphp-warn-performance.fixture.php")
    }

    fun testKphpSerialize() {
        doStubTest("kphp_stub/kphp-serialize.fixture.php")
    }
}
