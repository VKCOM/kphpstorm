package com.vk.kphpstorm.testing.tests

import com.intellij.mock.MockProject
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.helpers.toExPhpType

class ExPhpTypeTest : BasePlatformTestCase() {

    private fun createMockProject() = MockProject(null) {}

    fun testParsingFromPhpType() {
        val shouldBeEq = listOf(
            PhpType.INT,
            PhpType.CALLABLE,
            PhpType.VOID,
            PhpType().add("SomeClass"),
            PhpType().add("\\asdf\\NestedItem"),
            PhpType().add("false"),
            PhpType().add("\\false"),
            PhpType().add("float").add("int"),
            PhpType().add("\\any"),
            PhpType().add("int").add("false").pluralise()
        )

        shouldBeEq.forEach {
            assertEquals(it.toExPhpType()?.toPhpType(), it)
        }

        assertEquals(PhpType().add("\\kmixed").toExPhpType(), ExPhpType.KMIXED)
        assertEquals(PhpType().add("\\mixed").toExPhpType(), ExPhpType.ANY)

        PhpType().add("tuple(int, A)").toExPhpType().run {
            assertTrue(this is ExPhpTypeTuple)
            assertTrue((this as ExPhpTypeTuple).getSubkeyByIndex("1") is ExPhpTypeInstance)
            assertTrue((this.getSubkeyByIndex("1") as ExPhpTypeInstance).fqn == "A")
        }
    }

    fun testMixedIsAny() {
        assertEquals(PhpType.MIXED.toExPhpType(), ExPhpTypeAny())
        assertSame(PhpType.MIXED.toExPhpType(), ExPhpType.ANY)

        assertEquals(PhpType.MIXED.toExPhpType()?.toPhpType(), ExPhpTypeAny().toPhpType())
        assertSame(PhpType.MIXED.toExPhpType()?.toPhpType(), KphpPrimitiveTypes.PHP_TYPE_ANY)
    }

    fun testParsingFromString() {
        fun String.toExPhpType(): ExPhpType =
            PhpTypeToExPhpTypeParsing.parseFromString(this) ?: throw RuntimeException("Couldn't parse $this")

        "int".toExPhpType().apply {
            assertTrue(this is ExPhpTypePrimitive)
            assertSame(this, ExPhpType.INT)
        }
        "int|false".toExPhpType().apply {
            assertTrue(this is ExPhpTypePipe)
        }
        "int/null".toExPhpType().apply {
            assertTrue(this is ExPhpTypeNullable)
        }
        "?int".toExPhpType().apply {
            assertTrue(this is ExPhpTypeNullable)
        }
        "(int|false)[]".toExPhpType().apply {
            assertTrue(this is ExPhpTypeArray)
            assertTrue((this as ExPhpTypeArray).inner is ExPhpTypePipe)
        }
        "tuple|tuple(int, A)".toExPhpType().apply {
            assertTrue(this is ExPhpTypePipe)
            assertSame(getSubkeyByIndex("0"), ExPhpType.INT)
            assertTrue(getSubkeyByIndex("1") is ExPhpTypeInstance)
        }
        "int|tuple(int, A|null)".toExPhpType().apply {
            val i1 = getSubkeyByIndex("1")
            assertTrue(i1 is ExPhpTypeNullable)
            assertTrue((i1 as ExPhpTypeNullable).inner is ExPhpTypeInstance)
        }
        "int|tuple(int, A/int)".toExPhpType().apply {
            val i1 = getSubkeyByIndex("1")
            assertTrue(i1 is ExPhpTypePipe)
            assertTrue((i1 as ExPhpTypePipe).items[0] is ExPhpTypeInstance)
        }

    }

    fun testPluralizing() {
        // (int|false)[] generally speaking is not int[]|false[], but PhpType does exactly this :(
        // that's why ather phptype->ex we have pipe of arrays
        val phpType = PhpType().add("int").add("false").pluralise()
        val intFalseArr = phpType.toExPhpType()

        assertTrue(intFalseArr is ExPhpTypePipe)
        assertSame(((intFalseArr as ExPhpTypePipe).items[0] as ExPhpTypeArray).inner, ExPhpType.INT)
        assertSame((intFalseArr.items[1] as ExPhpTypeArray).inner, ExPhpType.FALSE)

        // since 2020.2, we can't assign 'int|false' to 'int': PhpStorm handles 'false' not as 'bool' now
        val intFalse = PhpType().add("int").add("false").toExPhpType()!!
        val intBare = PhpType.INT.toExPhpType()!!
        assertFalse(intBare.isAssignableFrom(intFalse, createMockProject()))

        val intArr = PhpType.INT.pluralise().toExPhpType()!!
        assertFalse(intArr.isAssignableFrom(intFalseArr, createMockProject()))
    }

    fun testForcing() {
        val phpType = PhpType().add("string").add("int").add("force(string)")

        assertTrue(PhpType.STRING.toExPhpType()!!.isAssignableFrom(phpType.toExPhpType()!!, createMockProject()))
    }

}
