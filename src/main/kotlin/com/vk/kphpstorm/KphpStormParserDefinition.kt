package com.vk.kphpstorm

import com.intellij.lang.*
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParserRegistry
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.parser.PhpParserDefinition
import com.jetbrains.php.lang.parser.PhpPsiElementCreator
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpUse
import com.vk.kphpstorm.exphptype.psi.*
import com.vk.kphpstorm.kphptags.ALL_KPHPDOC_TAGS
import com.vk.kphpstorm.kphptags.psi.*

/**
 * Main class that overrides php parsing PSI creation
 */
class KphpStormParserDefinition() : PhpParserDefinition() {
    init {
        // PhpDocTagParserRegistry.register() became deprecated in 2020.3
        // (the current suggestion is to use <docTagParserExtension> in plugin.xml)
        // custom @var/@param/@return handling is already done via xml description
        // but still use this for @kphp-tags: it's more handy, because we don't need to duplicate tag names to xml
        // when register() method is dropped, it won't compile and should be rewritten :(
        @Suppress("DEPRECATION")
        for (kphpDocTag in ALL_KPHPDOC_TAGS)
            PhpDocTagParserRegistry.register(kphpDocTag.nameWithAt, kphpDocTag.elementType.getTagParser())
    }

    /**
     * We have custom elementTypes, PhpStorm doesn't know how to create them.
     * So, override their psi impl creating.
     */
    override fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            KphpDocElementTypes.kphpDocTagSimple          -> KphpDocTagSimplePsiImpl(node)
            KphpDocElementTypes.kphpDocTagTemplateClass   -> KphpDocTagTemplateClassPsiImpl(node)
            KphpDocTplParameterDeclPsiImpl.elementType    -> KphpDocTplParameterDeclPsiImpl(node)
            KphpDocElementTypes.kphpDocTagWarnPerformance -> KphpDocTagWarnPerformancePsiImpl(node)
            KphpDocWarnPerformanceItemPsiImpl.elementType -> KphpDocWarnPerformanceItemPsiImpl(node)

            ExPhpTypePrimitivePsiImpl.elementType        -> ExPhpTypePrimitivePsiImpl(node)
            ExPhpTypeInstancePsiImpl.elementType         -> ExPhpTypeInstancePsiImpl(node)
            ExPhpTypePipePsiImpl.elementType             -> ExPhpTypePipePsiImpl(node)
            ExPhpTypeAnyPsiImpl.elementType              -> ExPhpTypeAnyPsiImpl(node)
            ExPhpTypeArrayPsiImpl.elementType            -> ExPhpTypeArrayPsiImpl(node)
            ExPhpTypeTuplePsiImpl.elementType            -> ExPhpTypeTuplePsiImpl(node)
            ExPhpTypeShapePsiImpl.elementType            -> ExPhpTypeShapePsiImpl(node)
            ExPhpTypeNullablePsiImpl.elementType         -> ExPhpTypeNullablePsiImpl(node)
            ExPhpTypeTplInstantiationPsiImpl.elementType -> ExPhpTypeTplInstantiationPsiImpl(node)
            ExPhpTypeCallablePsiImpl.elementType         -> ExPhpTypeCallablePsiImpl(node)
            ExPhpTypeForcingPsiImpl.elementType          -> ExPhpTypeForcingPsiImpl(node)

            else                                         -> PhpPsiElementCreator.create(node)
        }
    }
}

/**
 * This is just for plugin.xml: without this, <localInspection language="PHP" ...> highlighs "PHP" as red
 * This has no sense but correct plugin.xml validity while development
 */
class FakePhpLanguage : com.intellij.lang.Language("PHP")

class GenericsInstantiationInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context is PsiCommentImpl && context.elementType == PhpTokenTypes.C_STYLE_COMMENT && context.text.let { it.length > 6 && it[2] == '<' && it[it.length - 3] == '>' }) {
            val file = context.containingFile as? PhpFile ?: return
            val usesText = file.topLevelDefs.values().filterIsInstance<PhpUse>().joinToString("\n") { it.parent.text }

            val start = context.startOffset - context.textOffset
            val range = TextRange(start + 3, start + context.textLength - 3)
            registrar.startInjecting(PhpLanguage.INSTANCE)
                .addPlace("<?php\n${usesText}\n/**@var tuple(", ")*/", context, range)
                .doneInjecting()
        }
    }

    override fun elementsToInjectIn() = listOf(PsiCommentImpl::class.java)
}

class GenericsInstatiationFolding : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root is PhpFile) {
            val arr = mutableListOf<FoldingDescriptor>()
            val children = PsiTreeUtil.findChildrenOfType(root, PsiComment::class.java)
            for (child in children) {
                if (child.elementType == PhpTokenTypes.C_STYLE_COMMENT && child.text[2] == '<') {
                    val range = child.textRange
                    arr.add(FoldingDescriptor(child, range))
                }
            }
            return arr.toTypedArray()
        }

        return FoldingDescriptor.EMPTY
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return node.text.let { it.substring(2, it.length - 2) }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

}
