package com.vk.kphpstorm.kphptags.psi.serializers

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubSerializer
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl

/**
 * '@kphp-template-class T1, T2' has a separate elementType, psi for 'T1' and 'T2' and stub contents
 * @see KphpDocElementTypes.kphpDocTagTemplateClass
 */
class KphpDocTagTemplateClassElementTypeSerializer :
    PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagTemplateClass) {
    override fun serialize(stub: PhpDocTagStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.value)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): PhpDocTagStub {
        val name = dataStream.readName()?.toString()!!
        val stubValue = dataStream.readName()?.toString()
        return KphpDocTagStubImpl(parentStub, type, name, stubValue)
    }
}
