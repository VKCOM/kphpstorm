package com.vk.kphpstorm.kphptags.parser

import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.vk.kphpstorm.kphptags.psi.KphpDocTagSimpleElementType

class KphpDocTagSimpleParser : PhpDocTagParser() {
    override fun getElementType() = KphpDocTagSimpleElementType

    override fun parseContents(builder: PhpPsiBuilder) = true
}
