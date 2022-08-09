package com.vk.kphpstorm.testing.tests

import com.jetbrains.php.lang.inspections.codeSmell.PhpMethodOrClassCallIsNotCaseSensitiveInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class NativeInspectionTest : InspectionTestBase() {

    fun `test PhpMethodOrClassCallIsNotCaseSensitive inspection`() {
        myFixture.enableInspections(PhpMethodOrClassCallIsNotCaseSensitiveInspection())

        runFixture("native_inspection/case_sensitive.good.fixture.php")
    }

}
