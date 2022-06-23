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

object KphpDocTagJsonElementType : PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-json"), KphpDocTagElementType {
    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagJsonPsiImpl(stub, stub.stubType)
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

    override fun getTagParser() = object : PhpDocTagParser() {
        override fun getElementType() = KphpDocTagJsonElementType

        override fun parseContents(builder: PhpPsiBuilder): Boolean {
            while (true) {
                val marker = builder.mark()
                if (!builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)) {
                    marker.drop()
                    builder.error(PhpParserErrors.expected("Property name"))
                    break
                }

                if (builder.tokenText == null) {
                    break
                }

                if (builder.compare(PhpDocTokenTypes.DOC_TEXT) && builder.tokenText!!.startsWith("=")) {
                    var needNextIdentifier = true

                    if (builder.tokenText == "=") {
                        builder.compareAndEat(PhpDocTokenTypes.DOC_TEXT)
                    } else {
                        needNextIdentifier = false
                        builder.advanceLexer()
                    }

                    if (needNextIdentifier) {
                        if (!builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && !builder.compare(PhpDocTokenTypes.DOC_TEXT)) {
                            marker.drop()
                            builder.error(PhpParserErrors.expected("Property value"))
                            break
                        }

                        builder.advanceLexer()
                    }
                }

                marker.done(KphpDocJsonPropertyPsiImpl.elementType)
                return true

            }
            return true
        }
    }
}