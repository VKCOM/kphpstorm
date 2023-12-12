package com.vk.kphpstorm.kphptags.psi

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.stubs.PhpStubElementType

/**
 * '@kphp-template-class T1, T2' has a separate elementType, psi for 'T1' and 'T2' and stub contents
 * @see KphpDocElementTypes.kphpDocTagTemplateClass
 */
object KphpDocTagTemplateClassElementType : PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-template-class") {
    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagTemplateClassPsiImpl(stub, stub.stubType)
    }

    override fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        // stub value is 'T1,T2' — without spaces
        val stubValue = (psi as KphpDocTagTemplateClassPsiImpl).getTemplateArguments().joinToString(",")
        return KphpDocTagStubImpl(parentStub, this, psi.name, stubValue)
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
