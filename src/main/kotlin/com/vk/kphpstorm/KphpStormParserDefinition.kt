package com.vk.kphpstorm

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocParser
import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParserRegistry
import com.jetbrains.php.lang.parser.PhpParserDefinition
import com.jetbrains.php.lang.parser.PhpPsiElementCreator
import com.vk.kphpstorm.exphptype.psi.*
import com.vk.kphpstorm.kphptags.ALL_KPHPDOC_TAGS
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagSimplePsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagTemplateClassPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTplParameterDeclPsiImpl

/**
 * Main class that overrides php parsing PSI creation
 */
class KphpStormParserDefinition() : PhpParserDefinition() {
    init {
        PhpDocParser() // can not overload standard tags (@return etc) without this
        PhpDocTagParserRegistry.register("@return", PhpDocReturnTagParserEx())
        PhpDocTagParserRegistry.register("@param", PhpDocParamTagParserEx())
        PhpDocTagParserRegistry.register("@var", PhpDocVarTagParserEx())
        PhpDocTagParserRegistry.register("@type", PhpDocVarTagParserEx())

        for (kphpDocTag in ALL_KPHPDOC_TAGS)
            PhpDocTagParserRegistry.register(kphpDocTag.nameWithAt, kphpDocTag.elementType.getTagParser())
    }

    /**
     * We have custom elementTypes, PhpStorm doesn't know how to create them.
     * So, override their psi impl creating.
     */
    override fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            KphpDocElementTypes.kphpDocTagSimple         -> KphpDocTagSimplePsiImpl(node)
            KphpDocElementTypes.kphpDocTagTemplateClass  -> KphpDocTagTemplateClassPsiImpl(node)
            KphpDocTplParameterDeclPsiImpl.elementType   -> KphpDocTplParameterDeclPsiImpl(node)

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



