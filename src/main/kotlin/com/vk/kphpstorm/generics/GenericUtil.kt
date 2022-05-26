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
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

object GenericUtil {
    fun Function.isGeneric() = docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() != null

    fun PhpClass.isGeneric() = docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() != null

    fun ExPhpType.isGeneric() = toString().contains("%")

    fun PhpType.isGeneric(f: Function) = isGeneric(f.genericNames())

    fun PhpType.isGeneric(c: PhpClass) = isGeneric(c.genericNames())

    fun PhpType.isGeneric(genericNames: List<KphpDocGenericParameterDecl>): Boolean {
        // TODO: Подумать как сделать улучшить
        return genericNames.any { decl -> types.contains("%${decl.name}") } ||
                genericNames.any { decl -> types.any { type -> type.contains("%${decl.name}") } }
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

    fun ExPhpType.isGenericPipe(): Boolean {
        if (this is ExPhpTypePipe) {
            if (this.items.size != 2) return false
            return this.items.any {
                it is ExPhpTypeTplInstantiation || (it is ExPhpTypeArray && it.inner is ExPhpTypeTplInstantiation)
            }
        }

        return false
    }

    fun ExPhpType.getGenericPipeType(): ExPhpType? {
        if (!isGenericPipe()) {
            return null
        }

        return (this as ExPhpTypePipe).items.firstOrNull {
            // TODO:
            it is ExPhpTypeTplInstantiation || (it is ExPhpTypeArray && it.inner is ExPhpTypeTplInstantiation)
        }
    }

    /**
     * for IDE, we return PhpType "A"|"A<T>", that's why
     * A<A<T>> is resolved as "A"|"A<A/A<T>>", so if pipe — search for instantiation
     */
    fun ExPhpType.getInstantiation(): ExPhpTypeTplInstantiation? {
        return when (this) {
            is ExPhpTypePipe -> items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> inner
            is ExPhpTypeForcing -> inner
            else -> this
        } as? ExPhpTypeTplInstantiation
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
        } ?: return null

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
