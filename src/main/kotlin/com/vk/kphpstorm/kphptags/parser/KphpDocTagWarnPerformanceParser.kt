package com.vk.kphpstorm.kphptags.parser

import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser
import com.jetbrains.php.lang.parser.PhpParserErrors
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.vk.kphpstorm.kphptags.psi.KphpDocTagWarnPerformanceElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocWarnPerformanceItemPsiImpl

class KphpDocTagWarnPerformanceParser : PhpDocTagParser() {
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
                builder.error(PhpParserErrors.expected("'all' or 'some' or '!some'"))
                break
            }
            marker.done(KphpDocWarnPerformanceItemPsiImpl.elementType)
        } while (true)
        return true
    }
}
