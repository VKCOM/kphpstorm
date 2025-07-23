package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.intentions.PrettifyPhpdocBlockIntention
import com.vk.kphpstorm.testing.infrastructure.IntentionTestBase

class PhpDocPrettificationTest : IntentionTestBase(PrettifyPhpdocBlockIntention()) {

    fun testPrettify1() {
        runIntention("kphpdoc_inspection/prettify-intention-1.fixture.php")
    }

    fun testPrettify2() {
        runIntention("kphpdoc_inspection/prettify-intention-2.fixture.php")
    }

    fun testPrettify3() {
        runIntention("kphpdoc_inspection/prettify-intention-3.fixture.php")
    }

    fun testPrettify4() {
        runIntention("kphpdoc_inspection/prettify-intention-4.fixture.php")
    }

    fun testPrettify5() {
        runIntention("kphpdoc_inspection/prettify-intention-5.fixture.php")
    }

}
