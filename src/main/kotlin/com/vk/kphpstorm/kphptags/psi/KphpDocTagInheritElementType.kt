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
import com.vk.kphpstorm.exphptype.psi.TokensToExPhpTypePsiParsing

/**
 * '@kphp-inherit ExtendsClass<Type>' has a separate elementType,
 * psi for ExtendsClass<Type> and stub contents.
 *
 * @see KphpDocElementTypes.kphpDocTagInherit
 */
object KphpDocTagInheritElementType :
    PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-inherit"), KphpDocTagElementType {

    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagInheritPsiImpl(stub, stub.stubType)
    }

    override fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        // stub value is 'T1,T2:ExtendsClass,T2=default' — without spaces
        // TODO: add stubs
        return KphpDocTagStubImpl(parentStub, this, psi.name, "stubValue")
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
     * Parse tag argument - 'ExtendsClass<Type>, ImplementsClass<Type>' — making
     * ExtendsClass<Type> and ImplementsClass<Type> separate psi elements
     * @see KphpDocInheritParameterDeclPsiImpl
     */
    @Suppress("UnstableApiUsage")
    override fun getTagParser() = object : PhpDocTagParser() {
        override fun getElementType() = KphpDocTagInheritElementType

        override fun parseContents(builder: PhpPsiBuilder): Boolean {
            do {
                val marker = builder.mark()

                if (!TokensToExPhpTypePsiParsing.parseTypeExpression(builder)) {
                    marker.drop()
                    builder.error(PhpParserErrors.expected("Extends/implements class name"))
                    break
                }

                marker.done(KphpDocInheritParameterDeclPsiImpl.elementType)
            } while (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA))
            return true
        }
    }
}
