package com.vk.kphpstorm

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParserRegistry
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.parser.PhpParserDefinition
import com.jetbrains.php.lang.parser.PhpPsiElementCreator
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
            KphpDocElementTypes.kphpDocTagSimple           -> KphpDocTagSimplePsiImpl(node)
            KphpDocElementTypes.kphpDocTagTemplateClass    -> KphpDocTagTemplateClassPsiImpl(node)
            KphpDocElementTypes.kphpDocTagGeneric          -> KphpDocTagGenericPsiImpl(node)
            KphpDocGenericParameterDeclPsiImpl.elementType -> KphpDocGenericParameterDeclPsiImpl(node)
            KphpDocGenericParameterDeclPsiImpl.extendsElementType -> PhpDocTypeImpl(node)
            KphpDocElementTypes.kphpDocTagWarnPerformance  -> KphpDocTagWarnPerformancePsiImpl(node)
            KphpDocWarnPerformanceItemPsiImpl.elementType  -> KphpDocWarnPerformanceItemPsiImpl(node)

            ExPhpTypePrimitivePsiImpl.elementType          -> ExPhpTypePrimitivePsiImpl(node)
            ExPhpTypeInstancePsiImpl.elementType           -> ExPhpTypeInstancePsiImpl(node)
            ExPhpTypePipePsiImpl.elementType               -> ExPhpTypePipePsiImpl(node)
            ExPhpTypeAnyPsiImpl.elementType                -> ExPhpTypeAnyPsiImpl(node)
            ExPhpTypeArrayPsiImpl.elementType              -> ExPhpTypeArrayPsiImpl(node)
            ExPhpTypeTuplePsiImpl.elementType              -> ExPhpTypeTuplePsiImpl(node)
            ExPhpTypeShapePsiImpl.elementType              -> ExPhpTypeShapePsiImpl(node)
            ExPhpTypeNullablePsiImpl.elementType           -> ExPhpTypeNullablePsiImpl(node)
            ExPhpTypeTplInstantiationPsiImpl.elementType   -> ExPhpTypeTplInstantiationPsiImpl(node)
            ExPhpTypeCallablePsiImpl.elementType           -> ExPhpTypeCallablePsiImpl(node)
            ExPhpTypeClassStringPsiImpl.elementType        -> ExPhpTypeClassStringPsiImpl(node)
            ExPhpTypeForcingPsiImpl.elementType            -> ExPhpTypeForcingPsiImpl(node)

            PhpTokenTypes.C_STYLE_COMMENT                  -> {
                PhpDocTypeImpl(node)
            }

            else                                           -> PhpPsiElementCreator.create(node)
        }
    }
}

/**
 * This is just for plugin.xml: without this, <localInspection language="PHP" ...> highlighs "PHP" as red
 * This has no sense but correct plugin.xml validity while development
 */
class FakePhpLanguage : com.intellij.lang.Language("PHP")
