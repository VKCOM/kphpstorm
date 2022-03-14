package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

object GenericFunctionUtil {
    fun Function.isGeneric(): Boolean {
        return docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() != null
    }

    fun PhpType.isGeneric(f: Function): Boolean {
        val genericNames = f.genericNames()
        return isGeneric(genericNames)
    }

    fun PhpType.isGeneric(genericNames: List<String>): Boolean {
        // TODO: Подумать как сделать улучшить
        return genericNames.any { name -> types.contains("%$name") } ||
                genericNames.any { types.first().contains("%$it") }
    }

    fun Function.isReturnGeneric(): Boolean {
        if (docComment == null) return false

        return docComment!!.getTagElementsByName("@return").any {
            it.type.isGeneric(this)
        }
    }

    fun Function.genericNames(): List<String> {
        val docT = docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() as? KphpDocTagGenericPsiImpl
            ?: return emptyList()
        return docT.getGenericArguments()
    }

    fun nameIsGeneric(el: PsiElement, text: String): Boolean {
        var parent = el.parent
        while (parent !is PsiFile) {
            // например, когда мы внутри @param T, то когда-то дойдём до doc comment, где написано @kphp-generic
            if (parent is PhpDocComment) {
                val doc = parent
                for (child in doc.children) {
                    if (child is KphpDocTagGenericPsiImpl && child.getGenericArguments().contains(text)) {
                        return true
                    }
                }
            }
            // например, когда мы внутри @var T, то когда дойдём до функции/класса вверх, @kphp-generic будет в нём
            if (parent is Function) {
                val doc = PsiTreeUtil.skipWhitespacesBackward(parent) as? PhpDocComment
                if (doc != null) {
                    for (child in doc.children) {
                        if (child is KphpDocTagGenericPsiImpl && child.getGenericArguments().contains(text)) {
                            return true
                        }
                    }
                }
            }
            parent = parent.parent
        }

        return false
    }
}
