package com.vk.kphpstorm.kphptags.parser

import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser
import com.jetbrains.php.lang.parser.PhpParserErrors
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.vk.kphpstorm.kphptags.psi.KphpDocTagTemplateClassElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocTplParameterDeclPsiImpl

class KphpDocTagTemplateClassParser : PhpDocTagParser() {
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
