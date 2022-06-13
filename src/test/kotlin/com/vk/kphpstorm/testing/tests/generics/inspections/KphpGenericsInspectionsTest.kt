package com.vk.kphpstorm.testing.tests.generics.inspections

import com.vk.kphpstorm.inspections.KphpGenericsInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class KphpGenericsInspectionsTest : InspectionTestBase(KphpGenericsInspection()) {
    fun testBoundViolations() {
        runFixture("generics/inspections/bound_violation.fixture.php")
    }

    fun testNoEnoughInformation() {
        runFixture("generics/inspections/no_enough_information.fixture.php")
    }

    fun testInstantiationArgsMismatch() {
        runFixture("generics/inspections/instantiation_args_mismatch.fixture.php")
    }

    fun testSeveralReifiedTypes() {
        runFixture("generics/inspections/several_reified_types.fixture.php")
    }

    fun testDuplicateGenericParameters() {
        runFixture("generics/inspections/duplicate_generic_params.fixture.php")
    }

    fun testExtendsTypes() {
        runFixture("generics/inspections/extends/ok.fixture.php")
        runFixture("generics/inspections/extends/wrong.fixture.php")
    }
}
