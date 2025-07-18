package com.vk.kphpstorm.kphptags.psi.serializers

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubSerializer
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagTemplateClassPsiImpl

class KphpDocTagTemplateClassElementTypeSerializer :
    PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagTemplateClass) {
    fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagTemplateClassPsiImpl(stub, stub.stubType)
    }

    fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        // stub value is 'T1,T2' — without spaces
        val stubValue = (psi as KphpDocTagTemplateClassPsiImpl).getTemplateArguments().joinToString(",")
        return KphpDocTagStubImpl(parentStub, type, psi.name, stubValue)
    }

    override fun serialize(stub: PhpDocTagStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.value)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): PhpDocTagStub {
        val name = dataStream.readName()?.toString() ?: throw NullPointerException()
        val stubValue = dataStream.readName()?.toString()
        return KphpDocTagStubImpl(parentStub, type, name, stubValue)
    }
}
