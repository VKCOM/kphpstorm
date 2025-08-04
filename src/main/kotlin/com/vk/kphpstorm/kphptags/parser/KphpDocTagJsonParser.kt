package com.vk.kphpstorm.kphptags.parser

import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocParamTagParser
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser
import com.jetbrains.php.lang.parser.PhpParserErrors
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.vk.kphpstorm.exphptype.psi.TokensToExPhpTypePsiParsing
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocJsonAttributePsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocJsonForEncoderPsiImpl

class KphpDocTagJsonParser : PhpDocTagParser() {
    override fun getElementType() = KphpDocElementTypes.kphpDocTagJson

    override fun parseContents(builder: PhpPsiBuilder): Boolean {
        val paramsTagParser = PhpDocParamTagParser()

        while (true) {
            val forMarker = builder.mark()
            var needForIdentifier = false
            if (builder.compare(DOC_IDENTIFIER) && builder.tokenText == "for") {
                builder.advanceLexer()

                if (TokensToExPhpTypePsiParsing.parseTypeExpression(builder)) {
                    needForIdentifier = true
                } else {
                    forMarker.drop()
                    builder.error(PhpParserErrors.expected("JsonEncoder name"))
                    break
                }
            }
            if (needForIdentifier) {
                forMarker.done(KphpDocJsonForEncoderPsiImpl.elementType)
            } else {
                forMarker.drop()
            }

            val attributeName: String?
            val marker = builder.mark()
            if (builder.compare(DOC_IDENTIFIER)) {
                attributeName = builder.tokenText
                builder.advanceLexer()
            } else {
                marker.drop()
                builder.error(PhpParserErrors.expected("Attribute name"))
                break
            }

            if (builder.compare(DOC_TEXT) && builder.tokenText!!.startsWith("=")) {
                var needNextIdentifier = true

                if (builder.tokenText == "=") {
                    builder.compareAndEat(DOC_TEXT)
                } else {
                    needNextIdentifier = false
                    builder.advanceLexer()
                }

                if (needNextIdentifier) {
                    if (attributeName == "fields") {
                        while (true) {
                            if (!builder.compareAndEat(DOC_COMMA) && !paramsTagParser.parseContents(builder)) {
                                break
                            }
                        }
                    } else {
                        if (!builder.compare(DOC_IDENTIFIER) && !builder.compare(DOC_TEXT)) {
                            marker.drop()
                            builder.error(PhpParserErrors.expected("Attribute value"))
                            break
                        }

                        builder.advanceLexer()
                    }
                }
            }

            marker.done(KphpDocJsonAttributePsiImpl.elementType)
            return true

        }
        return true
    }
}
