package com.vk.kphpstorm.generics.psi

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiComment
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.PhpFile
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeTuple
import com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing

/**
 * Comment like `/*<T[, T2, ...]>*/` between function/method/class name, and the
 * argument list for a call.
 *
 * This comment stores a comma-separated list of explicit generic types.
 * Comment can have any types that PhpStorm can represent in phpdoc.
 *
 * The comment has no internal structure, since [PsiComment] can't
 * have children, as it's a leaf of the tree.
 */
class GenericInstantiationPsiCommentImpl(type: IElementType, text: CharSequence) : PsiCommentImpl(type, text) {
    /**
     * Returns the types defined in the comment.
     *
     * Example:
     *
     *    `/*<T, T2>*/` -> `[T, T2]`
     */
    fun instantiationTypes(): List<ExPhpType> {
        val outerFile = containingFile as PhpFile
        val injected = InjectedLanguageManager.getInstance(project)
            .findInjectedElementAt(outerFile, startOffset + 3)
            ?: return emptyList()

        val docTagGenericsInstantiation = injected.parentOfType<PhpDocTag>()
            ?: return emptyList()

        val specList = PhpTypeToExPhpTypeParsing.parse(docTagGenericsInstantiation.type) as? ExPhpTypeTuple
            ?: return emptyList()

        return specList.items
    }
}
