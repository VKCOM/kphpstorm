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

object KphpDocTagWarnPerformanceElementType : PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-warn-performance"), KphpDocTagElementType {
    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagWarnPerformancePsiImpl(stub, stub.stubType)
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


    /**
     * Parse tag argument - 'all !implicit-array-cast' — making them separate psi elements
     * @see KphpDocWarnPerformanceItemPsiImpl
     */
    override fun getTagParser() = object : PhpDocTagParser() {
        override fun getElementType() = KphpDocTagWarnPerformanceElementType

        override fun parseContents(builder: PhpPsiBuilder): Boolean {
            do {
                if (builder.compare(PhpDocTokenTypes.DOC_LEADING_ASTERISK) || builder.compare(PhpDocTokenTypes.DOC_COMMENT_END))
                    break

                val marker = builder.mark()
                val isNegation = builder.compare(PhpDocTokenTypes.DOC_TEXT) && builder.tokenText == "!"
                if (isNegation)
                    builder.advanceLexer()

                if (!builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)) {
                    marker.drop()
                    builder.error(PhpParserErrors.expected("[KPHP] 'all' or 'some' or '!some'"))
                    break
                }
                marker.done(KphpDocWarnPerformanceItemPsiImpl.elementType)
            } while (true)
            return true
        }
    }
}

