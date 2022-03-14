package com.vk.kphpstorm.highlighting

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * Custom 'Type info' handler, replacement of native
 * @see com.jetbrains.php.actions.PhpExpressionTypeProvider
 * The only purpose is to overload getInformationHint().
 *
 * Important! In plugin.xml, order="last", not "first" (seems like a bug in idea).
 */
class KphpStormTypeInfoProvider : ExpressionTypeProvider<PhpTypedElement>() {
    private val delegate = com.jetbrains.php.actions.PhpExpressionTypeProvider()

    override fun getInformationHint(element: PhpTypedElement): String {
        val phpType = element.type.global(element.project)
        return phpType.toExPhpType()?.let { PsiToExPhpType.dropGenerics(it).toHumanReadable(element) } ?: phpType.toString()
    }

    override fun getExpressionsAt(elementAt: PsiElement): List<PhpTypedElement> {
        return delegate.getExpressionsAt(elementAt)
    }

    override fun getErrorHint(): String {
        return delegate.errorHint
    }
}
