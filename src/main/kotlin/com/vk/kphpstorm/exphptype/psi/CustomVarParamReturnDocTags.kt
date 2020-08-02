package com.vk.kphpstorm.exphptype.psi

import com.intellij.psi.tree.IElementType
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser
import com.jetbrains.php.lang.parser.PhpPsiBuilder


/**
 * Custom parser of @return tag
 */
class PhpDocReturnTagParserEx : PhpDocTagParser() {
    override fun getElementType(): IElementType =
            PhpDocElementTypes.phpDocReturn

    override fun parseContents(builder: PhpPsiBuilder) =
            TokensToExPhpTypePsiParsing.parseTypeExpression(builder)
}


/**
 * Custom parser of @param tag
 */
class PhpDocParamTagParserEx : PhpDocTagParser() {
    override fun getElementType(): IElementType =
            PhpDocElementTypes.phpDocParam

    override fun parseContents(builder: PhpPsiBuilder) =
            TokensToExPhpTypePsiParsing.parseVarAndType(builder)
}


/**
 * Custom parser of @var tag
 */
class PhpDocVarTagParserEx : PhpDocTagParser() {
    override fun getElementType(): IElementType =
            PhpDocElementTypes.phpDocTag

    override fun parseContents(builder: PhpPsiBuilder) =
            TokensToExPhpTypePsiParsing.parseVarAndType(builder)
}

