package com.vk.kphpstorm.testing.tests.generics.general

import com.vk.kphpstorm.testing.infrastructure.GenericTestBase

class GenericsGeneralTest : GenericTestBase() {
    fun testNewExpr() {
        runFixture("generics/general/new_expr.fixture.php")
    }

    fun testFields() {
        runFixture("generics/general/fields/main.fixture.php")
        runFixture("generics/general/fields/static.fixture.php")
    }

    fun testCallback() {
        runFixture("generics/general/callback/return.fixture.php")
        runFixture("generics/general/callback/param.fixture.php")
    }

    fun testVectorUse() {
        runFixture(
            "generics/general/vector_use.fixture.php",
            "generics/Containers/Vector.php",
            "generics/Containers/Pair.php",
        )
    }

    fun testReifier() {
        runFixture("generics/general/reifier/reify.fixture.php")
    }

    fun testExtendsTypeReifier() {
        runFixture("generics/general/reifier/extends/classes_union.fixture.php")
        runFixture("generics/general/reifier/extends/primitives_union.fixture.php")
        runFixture("generics/general/reifier/extends/callable.fixture.php")
        runFixture("generics/general/reifier/extends/wrong.fixture.php")
    }

    fun testDefaultTypeReifier() {
        runFixture("generics/general/reifier/default/main.fixture.php")
        runFixture(
            "generics/general/reifier/default/wrong.fixture.php",
            "generics/Containers/Pair.php",
        )
        runFixture(
            "generics/general/reifier/default/generic.fixture.php",
            "generics/Containers/Vector.php",
            "generics/Containers/Pair.php",
        )
    }

    fun testReifyFromReturn() {
        runFixture(
            "generics/general/reifier/context/return.fixture.php",
            "generics/general/reifier/context/classes.php",
        )
        runFixture(
            "generics/general/reifier/context/return_wrong.fixture.php",
            "generics/general/reifier/context/classes.php",
        )
    }

    fun testReifyFromParam() {
        runFixture(
            "generics/general/reifier/context/param.fixture.php",
            "generics/general/reifier/context/classes.php"
        )
        runFixture(
            "generics/general/reifier/context/param_wrong.fixture.php",
            "generics/general/reifier/context/classes.php"
        )
    }

    fun testGenericInGeneric() {
        runFixture("generics/general/generic_in_generic.fixture.php")
    }

    fun testInheritTag() {
        runFixture("generics/general/inherit/tag/main.fixture.php")
    }

    fun testInherit() {
        runFixture("generics/general/inherit/simple_class.fixture.php")
        runFixture("generics/general/inherit/non_generic_child.fixture.php")
        runFixture("generics/general/inherit/class_and_interface.fixture.php")
    }

    /**
     * Disabled.
     *
     * See https://youtrack.jetbrains.com/issue/WI-67021/During-a-test-primitive-type-hints-are-resolved-as-instances
     */
    fun testFromKphp() {
//        val classes = arrayOf(
//            "generics/kphp/Classes/A.php",
//            "generics/kphp/Classes/B.php",
//            "generics/kphp/Classes/TemplateMagic.php",
//            "generics/kphp/Classes/TemplateMagicStatic.php",
//        )
//        runFixture(
//            "generics/kphp/016_kphp_param_depends_T.php",
//            *classes,
//        )
//
//        runFixture(
//            "generics/kphp/017_templates_primitives.php",
//            *classes,
//        )
//
//        runFixture(
//            "generics/kphp/018_classof_keyword.php",
//            *classes,
//        )
//
//        runFixture(
//            "generics/kphp/016_kphp_param_depends_T.php",
//            *classes,
//        )
//
//        runFixture(
//            "generics/kphp/016_kphp_param_depends_T.php",
//            *classes,
//        )
    }
}
