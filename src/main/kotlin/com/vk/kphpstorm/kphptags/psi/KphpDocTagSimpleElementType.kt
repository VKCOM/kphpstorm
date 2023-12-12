package com.vk.kphpstorm.kphptags.psi

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.stubs.PhpStubElementType

/**
 * '@kphp-...' tags that do not store anything to stubs and do not parse its argument as custom psi tree
 * are called 'simple'.
 * Storing nothing in stubs means that their contents/text MUST be accessed only within THE SAME FILE.
 * This is ok for annotators etc, but not ok for something influencing declarations for example.
 * @see KphpDocElementTypes.kphpDocTagSimple
 */
object KphpDocTagSimpleElementType : PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-...") {
    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagSimplePsiImpl(stub, stub.stubType)
    }

    override fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        return KphpDocTagStubImpl(parentStub, this, psi.name, null)
    }

    override fun serialize(stub: PhpDocTagStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.value)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): PhpDocTagStub {
        val name = dataStream.readName()?.toString() ?: throw NullPointerException()
        val stubValue = dataStream.readName()?.toString()
        return KphpDocTagStubImpl(parentStub, this, name, stubValue)
    }
}
