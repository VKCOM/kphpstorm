package com.vk.kphpstorm.kphptags.psi

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.parser.PhpParserErrors
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.jetbrains.php.lang.psi.stubs.PhpStubElementType

/**
 * '@kphp-template-class T1, T2' has a separate elementType, psi for 'T1' and 'T2' and stub contents
 * @see KphpDocElementTypes.kphpDocTagTemplateClass
 */
object KphpDocTagTemplateClassElementType : PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-template-class"), KphpDocTagElementType {
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


    /**
     * Parse tag argument - 'T1, T2' — making T1 and T2 separate psi elements
     * @see KphpDocTplParameterDeclPsiImpl
     */
    override fun getTagParser() = object : PhpDocTagParser() {
        override fun getElementType() = KphpDocTagTemplateClassElementType

        override fun parseContents(builder: PhpPsiBuilder): Boolean {
            do {
                val marker = builder.mark()
                if (!builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)) {
                    marker.drop()
                    builder.error(PhpParserErrors.expected("Template argument name (like T)"))
                    break
                }
                marker.done(KphpDocTplParameterDeclPsiImpl.elementType)
            } while (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA))
            return true
        }
    }
}
