package com.vk.kphpstorm.testing.tests

import com.vk.kphpstorm.inspections.NoTypeDeclarationInspection
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class NoTypeDeclarationInspectionTest : InspectionTestBase(NoTypeDeclarationInspection()) {

    fun testClassFields() {
        runFixture("typing_requirements/class_fields.fixture.php")
    }

    fun testMethodParameters() {
        runFixture("typing_requirements/class_method_parameters.fixture.php")
    }

    fun testMethodReturnValue() {
        runFixture("typing_requirements/class_method_return_value.fixture.php")
    }

    fun testFunctionParameters() {
        runFixture("typing_requirements/function_parameters.fixture.php")
    }

    fun testFunctionReturnValue() {
        runFixture("typing_requirements/function_return_value.fixture.php")
    }

    fun testFunctionLambdas() {
        runFixture("typing_requirements/function_lambdas.fixture.php")
    }

}
