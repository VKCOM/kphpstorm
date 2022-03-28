package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.NewExpressionImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

object GenericUtil {
    fun Function.isGeneric(): Boolean {
        return docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() != null
    }

    fun PhpClass.isGeneric(): Boolean {
        return docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() != null
    }

    fun PhpType.isGeneric(f: Function): Boolean {
        val genericNames = f.genericNames()
        return isGeneric(genericNames)
    }

    fun PhpType.isGeneric(c: PhpClass): Boolean {
        val genericNames = c.genericNames()
        return isGeneric(genericNames)
    }

    fun PhpType.isGeneric(genericNames: List<KphpDocGenericParameterDecl>): Boolean {
        // TODO: Подумать как сделать улучшить
        return genericNames.any { decl -> types.contains("%${decl.name}") } ||
                genericNames.any { decl -> types.any { type -> type.contains("%${decl.name}")  } }
    }

    fun Function.isReturnGeneric(): Boolean {
        if (docComment == null) return false

        return docComment!!.getTagElementsByName("@return").any {
            it.type.isGeneric(this)
        }
    }

    fun Function.genericNames(): List<KphpDocGenericParameterDecl> = genericNames(docComment)

    fun PhpClass.genericNames(): List<KphpDocGenericParameterDecl> = genericNames(docComment)

    private fun genericNames(docComment: PhpDocComment?): List<KphpDocGenericParameterDecl> {
        val docT = docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() as? KphpDocTagGenericPsiImpl
            ?: return emptyList()
        return docT.getGenericArgumentsWithExtends()
    }

    fun findInstantiationComment(el: PsiElement): GenericInstantiationPsiCommentImpl? {
        val candidate = when (el) {
            is NewExpressionImpl -> {
                el.classReference?.nextSibling
            }
            is MethodReferenceImpl -> {
                el.firstChild?.nextSibling?.nextSibling?.nextSibling
            }
            else -> {
                el.firstChild?.nextSibling
            }
        }

        if (candidate == null) return null
        if (candidate !is GenericInstantiationPsiCommentImpl) return null
        return candidate
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

            if (parent is PhpClass) {
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
