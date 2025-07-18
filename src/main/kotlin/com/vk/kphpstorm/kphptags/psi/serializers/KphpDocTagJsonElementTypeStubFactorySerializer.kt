package com.vk.kphpstorm.kphptags.psi.serializers

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubSerializer
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagJsonPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl

class KphpDocTagJsonElementTypeStubFactorySerializer : PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagJson) {
    override fun serialize(stub: PhpDocTagStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.value)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): PhpDocTagStub {
        val name = dataStream.readName()?.toString() ?: throw NullPointerException()
        val stubValue = dataStream.readName()?.toString()
        return KphpDocTagStubImpl(parentStub, type, name, stubValue)
    }

    fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagJsonPsiImpl(stub, stub.stubType)
    }

    fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        return KphpDocTagStubImpl(parentStub, type, psi.name, null)
    }
}
