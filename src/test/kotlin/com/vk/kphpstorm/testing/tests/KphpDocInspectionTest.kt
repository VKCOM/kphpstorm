package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.KphpDocInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpDocInspectionTest : InspectionTestBase(KphpDocInspection()) {

    fun testKphpInferDeprecated() {
        runFixture("kphpdoc_inspection/deprecated_kphp_infer.fixture.php")
    }

    fun testSerializable() {
        runFixture("kphpdoc_inspection/serializable.fixture.php")
    }

    fun testParamTags() {
        runFixture("kphpdoc_inspection/param_tags.fixture.php")
    }

    fun testFieldVarTags() {
        runFixture("kphpdoc_inspection/field_var_tags.fixture.php")
    }

    fun testReturnTags() {
        runFixture("kphpdoc_inspection/return_tags.fixture.php")
    }

    fun testSimplifyPhpdocVars() {
        runFixture("kphpdoc_inspection/simplify_phpdoc_vars.fixture.php")
    }

    fun testSimplifyPhpdocTypes() {
        runFixture("kphpdoc_inspection/simplify_phpdoc_types.fixture.php")
    }

    fun testSwapTypeVarName() {
        runFixture("kphpdoc_inspection/swap_type_and_var_name.fixture.php")
    }

    fun testKphpWarnPerformanceDocTag() {
        runFixture("kphpdoc_inspection/warn-performance.fixture.php")
    }

}
