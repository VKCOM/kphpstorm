package com.vk.kphpstorm

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.parser.PhpParserDefinition
import com.jetbrains.php.lang.parser.PhpPsiElementCreator
import com.vk.kphpstorm.exphptype.psi.*
import com.vk.kphpstorm.kphptags.psi.*

/**
 * Main class that overrides php parsing PSI creation
 */
class KphpStormParserDefinition : PhpParserDefinition() {

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
            KphpDocElementTypes.kphpDocTagJson            -> KphpDocTagJsonPsiImpl(node)
            KphpDocJsonAttributePsiImpl.elementType       -> KphpDocJsonAttributePsiImpl(node)
            KphpDocJsonForEncoderPsiImpl.elementType      -> KphpDocJsonForEncoderPsiImpl(node)

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
