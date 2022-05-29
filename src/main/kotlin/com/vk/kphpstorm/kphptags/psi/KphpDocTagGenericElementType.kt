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
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.exphptype.psi.TokensToExPhpTypePsiParsing

/**
 * '@kphp-generic T1, T2: ExtendsClass' has a separate elementType,
 * psi for 'T1' and 'T2: ExtendsClass' and stub contents.
 *
 * @see KphpDocElementTypes.kphpDocTagGeneric
 */
object KphpDocTagGenericElementType : PhpStubElementType<PhpDocTagStub, PhpDocTag>("@kphp-generic"),
    KphpDocTagElementType {
    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagGenericPsiImpl(stub, stub.stubType)
    }

    override fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        // stub value is 'T1,T2:ExtendsClass,T2=default' — without spaces
        val stubValue = (psi as KphpDocTagGenericPsiImpl).getGenericArgumentsWithExtends()
            .joinToString(",") {
                val type = StringBuilder()
                type.append(it.name)

                if (it.extendsClass != null) {
                    type.append(":")
                    type.append(it.extendsClass)
                }

                if (it.defaultType != null) {
                    type.append("=")
                    type.append(it.defaultType)
                }

                type.toString()
            }
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
     * @see KphpDocGenericParameterDeclPsiImpl
     */
    override fun getTagParser() = object : PhpDocTagParser() {
        override fun getElementType() = KphpDocTagGenericElementType

        override fun parseContents(builder: PhpPsiBuilder): Boolean {
            do {
                val marker = builder.mark()
                if (!builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)) {
                    marker.drop()
                    builder.error(PhpParserErrors.expected("Generic argument name (like T)"))
                    break
                }

                if (builder.compare(PhpDocTokenTypes.DOC_TEXT)) {
                    var text = builder.tokenText?.trim()
                    builder.advanceLexer()

                    var withExtends = false
                    if (text == ":") {
                        val extendsMarker = builder.mark()

                        if (!TokensToExPhpTypePsiParsing.parseTypeExpression(builder)) {
                            marker.drop()
                            extendsMarker.drop()
                            builder.error(PhpParserErrors.expected("Extends class name"))
                            break
                        }

//                        if (!builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && !builder.compare(PhpDocTokenTypes.DOC_NAMESPACE)) {
//                            marker.drop()
//                            extendsMarker.drop()
//                            builder.error(PhpParserErrors.expected("Extends class name"))
//                            break
//                        }
//
//                        Namespace.parseReference(builder)
//                        builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)
//
                        extendsMarker.done(ExPhpTypeInstancePsiImpl.elementType)
                        withExtends = true
                    }

                    if (withExtends) {
                        text = builder.tokenText?.trim()
                    }

                    if (text == "=") {
                        if (withExtends) {
                            builder.advanceLexer()
                        }
                        val defaultTypeMarker = builder.mark()

                        if (!TokensToExPhpTypePsiParsing.parseTypeExpression(builder)) {
                            marker.drop()
                            defaultTypeMarker.drop()
                            builder.error(PhpParserErrors.expected("Default type name"))
                            break
                        }

                        defaultTypeMarker.done(ExPhpTypeInstancePsiImpl.elementType)

//                        if (!builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && !builder.compare(PhpDocTokenTypes.DOC_NAMESPACE)) {
//                            marker.drop()
//                            defaultTypeMarker.drop()
//                            builder.error(PhpParserErrors.expected("Default type name"))
//                            break
//                        }
//
//                        if (KphpPrimitiveTypes.mapPrimitiveToPhpType.containsKey(builder.tokenText!!)) {
//                            builder.advanceLexer()
//                            defaultTypeMarker.done(ExPhpTypePrimitivePsiImpl.elementType)
//                        } else {
//                            Namespace.parseReference(builder)
//                            builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)
//
//                            defaultTypeMarker.done(ExPhpTypeInstancePsiImpl.elementType)
//                        }
                    }
                }
                marker.done(KphpDocGenericParameterDeclPsiImpl.elementType)
            } while (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA))
            return true
        }
    }
}
