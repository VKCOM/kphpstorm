package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.intentions.AddImmutableClassAnnotationIntention
import com.vk.kphpstorm.testing.infrastructure.IntentionTestBase

class AddImmutableAnnoIntentionTest : IntentionTestBase(AddImmutableClassAnnotationIntention()) {
    fun testAddImmutableAnno1() {
        runIntention("kphp_intentions/immutable_class_intention-1.fixture.php")
    }

    fun testAddImmutableAnno2() {
        assertNoIntention("kphp_intentions/immutable_class_intention-2.nointention.php")
    }

    fun testAddImmutableAnno3() {
        assertNoIntention("kphp_intentions/immutable_class_intention-3.nointention.php")
    }

    fun testAddImmutableAnno4() {
        assertNoIntention("kphp_intentions/immutable_class_intention-4.nointention.php")
    }

    fun testAddImmutableAnno5() {
        assertNoIntention("kphp_intentions/immutable_class_intention-5.nointention.php")
    }

    fun testAddImmutableAnno6() {
        runIntention("kphp_intentions/immutable_class_intention-6.fixture.php")
    }

    fun testAddImmutableAnno7() {
        runIntention("kphp_intentions/immutable_class_intention-7.fixture.php")
    }

    fun testAddImmutableAnno8() {
        runIntention("kphp_intentions/immutable_class_intention-8.fixture.php")
    }

    fun testAddImmutableAnno9() {
        runIntention("kphp_intentions/immutable_class_intention-9.fixture.php")
    }

    fun testAddImmutableAnno10() {
        runIntention("kphp_intentions/immutable_class_intention-10.fixture.php")
    }
}
