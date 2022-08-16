package com.vk.kphpstorm.testing.tests

import com.intellij.mock.MockProject
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.helpers.toExPhpType
import junit.framework.TestCase
import org.junit.Assert

class ExPhpTypeTest : TestCase() {

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
            Assert.assertEquals(it.toExPhpType()?.toPhpType(), it)
        }

        Assert.assertEquals(PhpType().add("\\kmixed").toExPhpType(), ExPhpType.KMIXED)
        Assert.assertEquals(PhpType().add("\\mixed").toExPhpType(), ExPhpType.ANY)

        PhpType().add("tuple(int, A)").toExPhpType().run {
            Assert.assertTrue(this is ExPhpTypeTuple)
            Assert.assertTrue((this as ExPhpTypeTuple).getSubkeyByIndex("1") is ExPhpTypeInstance)
            Assert.assertTrue((this.getSubkeyByIndex("1") as ExPhpTypeInstance).fqn == "A")
        }
    }

    fun testMixedIsAny() {
        Assert.assertEquals(PhpType.MIXED.toExPhpType(), ExPhpTypeAny())
        Assert.assertSame(PhpType.MIXED.toExPhpType(), ExPhpType.ANY)

        Assert.assertEquals(PhpType.MIXED.toExPhpType()?.toPhpType(), ExPhpTypeAny().toPhpType())
        Assert.assertSame(PhpType.MIXED.toExPhpType()?.toPhpType(), KphpPrimitiveTypes.PHP_TYPE_ANY)
    }

    fun testParsingFromString() {
        "int".toExPhpType().apply {
            Assert.assertTrue(this is ExPhpTypePrimitive)
            Assert.assertSame(this, ExPhpType.INT)
        }
        "int|false".toExPhpType().apply {
            Assert.assertTrue(this is ExPhpTypePipe)
        }
        "int/null".toExPhpType().apply {
            Assert.assertTrue(this is ExPhpTypeNullable)
        }
        "?int".toExPhpType().apply {
            Assert.assertTrue(this is ExPhpTypeNullable)
        }
        "(int|false)[]".toExPhpType().apply {
            Assert.assertTrue(this is ExPhpTypeArray)
            Assert.assertTrue((this as ExPhpTypeArray).inner is ExPhpTypePipe)
        }
        "tuple|tuple(int, A)".toExPhpType().apply {
            Assert.assertTrue(this is ExPhpTypePipe)
            Assert.assertSame(getSubkeyByIndex("0"), ExPhpType.INT)
            Assert.assertTrue(getSubkeyByIndex("1") is ExPhpTypeInstance)
        }
        "int|tuple(int, A|null)".toExPhpType().apply {
            val i1 = getSubkeyByIndex("1")
            Assert.assertTrue(i1 is ExPhpTypeNullable)
            Assert.assertTrue((i1 as ExPhpTypeNullable).inner is ExPhpTypeInstance)
        }
        "int|tuple(int, A/int)".toExPhpType().apply {
            val i1 = getSubkeyByIndex("1")
            Assert.assertTrue(i1 is ExPhpTypePipe)
            Assert.assertTrue((i1 as ExPhpTypePipe).items[0] is ExPhpTypeInstance)
        }

    }

    fun testPluralizing() {
        // (int|false)[] generally speaking is not int[]|false[], but PhpType does exactly this :(
        // that's why ather phptype->ex we have pipe of arrays
        val phpType = PhpType().add("int").add("false").pluralise()
        val intFalseArr = phpType.toExPhpType()

        Assert.assertTrue(intFalseArr is ExPhpTypePipe)
        Assert.assertSame(((intFalseArr as ExPhpTypePipe).items[0] as ExPhpTypeArray).inner, ExPhpType.INT)
        Assert.assertSame((intFalseArr.items[1] as ExPhpTypeArray).inner, ExPhpType.FALSE)

        // since 2020.2, we can't assign 'int|false' to 'int': PhpStorm handles 'false' not as 'bool' now
        val intFalse = PhpType().add("int").add("false").toExPhpType()!!
        val intBare = PhpType.INT.toExPhpType()!!
        Assert.assertFalse(intBare.isAssignableFrom(intFalse, createMockProject()))

        val intArr = PhpType.INT.pluralise().toExPhpType()!!
        Assert.assertFalse(intArr.isAssignableFrom(intFalseArr, createMockProject()))
    }

    fun testForcing() {
        val phpType = PhpType().add("string").add("int").add("force(string)")

        Assert.assertTrue(PhpType.STRING.toExPhpType()!!.isAssignableFrom(phpType.toExPhpType()!!, createMockProject()))
    }

    fun testSupperForcing() {
        checkDrop("int|string", "int|string")
        checkDrop("force(force(int[])[])", "int[][]")
        checkDrop("string|bool|force(int)", "int")
        checkDrop("string|bool|force(int)|force(float)", "int|float")
        checkDrop("force(force(int[])[])|force(null)", "int[][]|null")
        checkDrop("tuple(force(int)|string, int|force(string), bool)", "tuple(int,string,bool)")
        checkDrop("shape(key: int|force(string), key2:bool, key3: float|bool|force(int[][]))", "shape(key:string,key2:bool,key3:int[][])")
        checkDrop("callable(string,force(int)|string):force(force(int[])[])|string", "callable(string,int):int[][]")
        checkDrop("callable(int,force(?bool)|string)", "callable(int,?bool):void")
    }

    private fun String.toExPhpType(): ExPhpType =
        PhpTypeToExPhpTypeParsing.parseFromString(this) ?: throw RuntimeException("Couldnt parse $this")

    private fun checkDrop(forceType: String, expectedType: String) {
        forceType.toExPhpType().apply {
            Assert.assertEquals(expectedType, this.dropForce().toString())

        }
    }
}
