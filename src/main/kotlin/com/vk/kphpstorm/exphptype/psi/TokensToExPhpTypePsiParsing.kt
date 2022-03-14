package com.vk.kphpstorm.exphptype.psi

import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.parser.PhpParserErrors
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.jetbrains.php.lang.parser.parsing.Namespace
import com.vk.kphpstorm.exphptype.KphpPrimitiveTypes

/**
 * Custom parsing of @param/@var/@return — having only tokens (and PsiBuilder), make psi.
 * @see com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing doing the same but string->ExPhpType
 * Quite similar to phpdoc.cpp parsing logic in kphp.
 */
internal object TokensToExPhpTypePsiParsing {
    private fun PhpPsiBuilder.expected(s: String): Boolean {
        this.error(PhpParserErrors.expected(s))
        return true
    }

    // example: tuple(int[], A, \shared\Instance)
    private fun parseTupleContents(builder: PhpPsiBuilder): Boolean {
        if (!builder.compareAndEat(PhpDocTokenTypes.DOC_LPAREN) && !builder.compareAndEat(PhpDocTokenTypes.DOC_LAB))
            return !builder.expected("(")

        while (true) {
            if (!parseTypeExpression(builder))
                return builder.expected("expression")
            if (builder.compareAndEat(PhpDocTokenTypes.DOC_RPAREN) || builder.compareAndEat(PhpDocTokenTypes.DOC_RAB))
                return true

            if (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA))
                continue
            return builder.expected(", or )")
        }
    }

    // example: shape(x:int, y?:\A, z:tuple(...))
    // in the end there may be DOC_TEXT "...": shape(x:int, ...)
    private fun parseShapeContents(builder: PhpPsiBuilder): Boolean {
        if (!builder.compareAndEat(PhpDocTokenTypes.DOC_LPAREN) && !builder.compareAndEat(PhpDocTokenTypes.DOC_LAB))
            return !builder.expected("(")

        while (true) {
            if (!builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER))
                return builder.expected("key name")
            // val keyName = builder.tokenText
            builder.advanceLexer()

            val sepOk = builder.compare(PhpDocTokenTypes.DOC_TEXT) && builder.tokenText.let {
                it == ":" || it == "?:" || it == ":?"
            }
            if (sepOk)
                builder.advanceLexer()
            else
                return builder.expected(":")

            if (!parseTypeExpression(builder))
                return builder.expected("expression")
            if (builder.compareAndEat(PhpDocTokenTypes.DOC_RPAREN) || builder.compareAndEat(PhpDocTokenTypes.DOC_RAB))
                return true

            if (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA)) {
                if (builder.compare(PhpDocTokenTypes.DOC_TEXT) && builder.tokenText == "...") {
                    builder.advanceLexer()
                    if (!builder.compareAndEat(PhpDocTokenTypes.DOC_RPAREN) && !builder.compareAndEat(PhpDocTokenTypes.DOC_RAB))
                        return builder.expected(")")
                    return true
                }
                continue
            }
            return builder.expected(", or )")
        }
    }

    private fun parseGenericSpecialization(builder: PhpPsiBuilder): Boolean {
        if (!builder.compareAndEat(PhpDocTokenTypes.DOC_LAB))
            return !builder.expected("<")

        while (true) {
            if (!parseTypeExpression(builder))
                return builder.expected("expression")
            if (builder.compareAndEat(PhpDocTokenTypes.DOC_RAB))
                return true

            if (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA))
                continue
            return builder.expected(", or >")
        }
    }

    private fun parseTypedCallableContents(builder: PhpPsiBuilder): Boolean {
        if (!builder.compareAndEat(PhpDocTokenTypes.DOC_LPAREN))
            return !builder.expected("(")

        while (true) {
            if (builder.compareAndEat(PhpDocTokenTypes.DOC_RPAREN))
                break
            if (!parseTypeExpression(builder))
                return builder.expected("expression")

            if (builder.compareAndEat(PhpDocTokenTypes.DOC_COMMA))
                continue
            if (!builder.compare(PhpDocTokenTypes.DOC_RPAREN))
                return builder.expected(")")
        }

        if (builder.compare(PhpDocTokenTypes.DOC_TEXT) && builder.tokenText == ":") {
            builder.advanceLexer()
            if (!parseTypeExpression(builder))
                return builder.expected("expression")
        }

        return true
    }

    private fun parseForcingTypeContents(builder: PhpPsiBuilder): Boolean {
        if (!builder.compareAndEat(PhpDocTokenTypes.DOC_LPAREN))
            return !builder.expected("(")

        if (!parseTypeExpression(builder))
            return builder.expected("expression")

        if (!builder.compareAndEat(PhpDocTokenTypes.DOC_RPAREN))
            return builder.expected(")")
        return true
    }

    private fun parseSimpleType(builder: PhpPsiBuilder): Boolean {
        if (builder.compareAndEat(PhpDocTokenTypes.DOC_LPAREN)) {
            if (!parseTypeExpression(builder))
                return false
            builder.match(PhpDocTokenTypes.DOC_RPAREN)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_TEXT) && builder.tokenText == "?") {
            val marker = builder.mark()
            builder.advanceLexer()
            if (!parseTypeExpression(builder)) {
                marker.drop()
                return false
            }
            marker.done(ExPhpTypeNullablePsiImpl.elementType)
            return true
        }

        // once we get rid of syntax \tuple, but for now be must handle both tuple and \tuple (and \shape)
        // using rawLookup() we can know only elementType, not tokenText, so use heruistics
        val seemsLikeSlashTupleOrShape =
                builder.compare(PhpDocTokenTypes.DOC_NAMESPACE)
                        && builder.rawLookup(1) == PhpDocTokenTypes.DOC_IDENTIFIER
                        && builder.rawLookup(2).let { it == PhpDocTokenTypes.DOC_LPAREN || it == PhpDocTokenTypes.DOC_LAB }
        if (seemsLikeSlashTupleOrShape)
            builder.advanceLexer()

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && builder.tokenText == "tuple") {
            val marker = builder.mark()
            builder.advanceLexer()
            if (!parseTupleContents(builder)) {
                marker.drop()
                return false
            }
            marker.done(ExPhpTypeTuplePsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && builder.tokenText == "shape") {
            val marker = builder.mark()
            builder.advanceLexer()
            if (!parseShapeContents(builder)) {
                marker.drop()
                return false
            }
            marker.done(ExPhpTypeShapePsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && builder.tokenText == "any") {
            val marker = builder.mark()
            builder.advanceLexer()
            marker.done(ExPhpTypeAnyPsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && builder.tokenText == "class-string") {
            val marker = builder.mark()
            builder.advanceLexer()
            if (!parseGenericSpecialization(builder)) {
                marker.drop()
                return false
            }
            marker.done(ExPhpTypeClassStringPsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && builder.tokenText == "callable" && builder.rawLookup(1) == PhpDocTokenTypes.DOC_LPAREN) {
            val marker = builder.mark()
            builder.advanceLexer()
            if (!parseTypedCallableContents(builder)) {
                marker.drop()
                return false
            }
            marker.done(ExPhpTypeCallablePsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && builder.tokenText == "force" && builder.rawLookup(1) == PhpDocTokenTypes.DOC_LPAREN) {
            val marker = builder.mark()
            builder.advanceLexer()
            if (!parseForcingTypeContents(builder)) {
                marker.drop()
                return false
            }
            marker.done(ExPhpTypeForcingPsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) && KphpPrimitiveTypes.mapPrimitiveToPhpType.containsKey(builder.tokenText!!)) {
            val marker = builder.mark()
            builder.advanceLexer()
            marker.done(ExPhpTypePrimitivePsiImpl.elementType)
            return true
        }

        if (builder.compare(PhpDocTokenTypes.DOC_IDENTIFIER) || builder.compare(PhpDocTokenTypes.DOC_NAMESPACE)) {
            val marker = builder.mark()
            Namespace.parseReference(builder)
            builder.compareAndEat(PhpDocTokenTypes.DOC_IDENTIFIER)
            marker.done(ExPhpTypeInstancePsiImpl.elementType)

            if (builder.compare(PhpDocTokenTypes.DOC_LAB)) {
                val instantiationMarker = marker.precede()
                if (!parseGenericSpecialization(builder))
                    instantiationMarker.drop()
                else
                    instantiationMarker.done(ExPhpTypeTplInstantiationPsiImpl.elementType)
            }

            return true
        }

        return false
    }

    private fun parseTypeArray(builder: PhpPsiBuilder): Boolean {
        var array = builder.mark()
        if (!parseSimpleType(builder)) {
            array.drop()
            return false
        }
        while (builder.compare(PhpDocTokenTypes.DOC_LBRACKET) && builder.rawLookup(1) == PhpDocTokenTypes.DOC_RBRACKET) {
            builder.advanceLexer()
            builder.advanceLexer()
            array.done(ExPhpTypeArrayPsiImpl.elementType)
            array = array.precede()
        }
        array.drop()
        return true
    }

    fun parseTypeExpression(builder: PhpPsiBuilder): Boolean {
        val marker = builder.mark()
        if (!parseTypeArray(builder)) {
            marker.drop()
            return false
        }
        // wrap with exPhpTypePipe only 'T1|T2', leaving 'T' being as is
        if (!builder.compare(PhpDocElementTypes.DOC_PIPE)) {
            marker.drop()
            return true
        }

        while (builder.compareAndEat(PhpDocElementTypes.DOC_PIPE)) {
            if (!parseTypeArray(builder))
                break
        }
        marker.done(ExPhpTypePipePsiImpl.elementType)
        return true
    }

    private fun parseVar(builder: PhpPsiBuilder): Boolean {
        val tag = builder.mark()
        if (builder.compare(PhpDocTokenTypes.DOC_TEXT) && "..." == builder.tokenText) {
            builder.advanceLexer()
        }

        if (builder.compareAndEat(PhpDocTokenTypes.DOC_VARIABLE)) {
            tag.done(PhpDocElementTypes.phpDocVariable)
            return true
        }

        tag.rollbackTo()
        return false
    }

    fun parseVarAndType(builder: PhpPsiBuilder): Boolean {
        if (parseVar(builder)) {
            parseTypeExpression(builder)
            return true
        }

        val start = builder.mark()
        if (parseTypeExpression(builder) && parseVar(builder)) {
            start.drop()
            return true
        }
        start.rollbackTo()
        parseTypeExpression(builder)
        return false
    }
}
