package com.vk.kphpstorm

import com.intellij.lang.ASTFactory
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.ILazyParseableElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocTokenImpl
import com.jetbrains.php.lang.lexer.PhpTokenTypes

class KphpStormASTFactory : MyDefaultASTFactoryImpl() {
    override fun createComment(type: IElementType, text: CharSequence): LeafElement {
        if (!text.startsWith("/*<") && !text.endsWith(">*/")) {
            return PsiCommentImpl(type, text)
        }

        return PhpDocTokenImpl(type, text)
    }
}

abstract class MyDefaultASTFactoryImpl : ASTFactory() {
    override fun createLazy(type: ILazyParseableElementType, text: CharSequence?): LazyParseableElement {
        if (type == PhpTokenTypes.C_STYLE_COMMENT) {
            return LazyParseableElement(type, text)
        }

        return if (type is IFileElementType) {
            FileElement(type, text)
        } else LazyParseableElement(type, text)
    }

    override fun createComposite(type: IElementType): CompositeElement {
        if (type == PhpTokenTypes.C_STYLE_COMMENT) {
            return CompositeElement(type)
        }

        return if (type is IFileElementType) {
            FileElement(type, null)
        } else CompositeElement(type)
    }

    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement {
        val lang = type.language
        val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang)
        if (parserDefinition != null) {
            if (parserDefinition.commentTokens.contains(type)) {
                return createComment(type, text)
            }
        }
        return LeafPsiElement(type, text)
    }

    open fun createComment(type: IElementType, text: CharSequence): LeafElement {
        return PsiCommentImpl(type, text)
    }
}
