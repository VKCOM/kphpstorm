package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.NewExpressionImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

object GenericUtil {
    fun PhpNamedElement.isGeneric() = docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull() != null

    fun ExPhpType.isGeneric() = toString().contains("%")

    fun PhpType.isGeneric(f: PhpNamedElement) = isGeneric(f.genericNames())

    fun PhpType.isGeneric(genericNames: List<KphpDocGenericParameterDecl>): Boolean {
        // TODO: Подумать как сделать улучшить
        return genericNames.any { decl -> types.contains("%${decl.name}") } ||
                genericNames.any { decl -> types.any { type -> type.contains("%${decl.name}") } }
    }

    fun Field.isGeneric(): Boolean {
        if (docComment == null) return false
        val varTag = docComment?.varTag ?: return false
        val klass = containingClass ?: return false
        return varTag.type.isGeneric(klass)
    }

    fun Function.isReturnGeneric(): Boolean {
        if (docComment == null) return false

        return docComment!!.getTagElementsByName("@return").any { tag ->
            tag.type.isGeneric(this)
        }
    }

    fun PhpNamedElement.genericNames(): List<KphpDocGenericParameterDecl> = genericNames(docComment)

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

    fun ExPhpType.getGenericTypeOrSelf(): ExPhpType {
        if (this is ExPhpTypePipe) {
            return this.items.firstOrNull {
                it is ExPhpTypeGenericsT
            } as? ExPhpTypeGenericsT ?: return this
        }

        return this
    }

    fun ExPhpTypePipe.isStringableStringUnion(): Boolean {
        if (items.size == 2) {
            return items.find { it is ExPhpTypeInstance && it.fqn.endsWith("\\Stringable") } != null &&
                    items.find { it is ExPhpTypePrimitive && it.typeStr == "string" } != null
        }

        return false
    }

    fun generateUniqueGenericName(names: List<KphpDocGenericParameterDecl>?): String {
        if (names == null || names.isEmpty()) return "T"

        for (i in 1..100) {
            val name = "T$i"
            if (names.none { it.name == name }) {
                return name
            }
        }

        return "T"
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
        if (el is Field) return null

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
            if (parent is PhpDocComment) {
                if (nameIsGenericInDoc(parent, text)) {
                    return true
                }
            }
            if (parent is Function) {
                if (nameIsGenericInDoc(parent.docComment, text)) {
                    return true
                }
            }

            if (parent is PhpClass) {
                if (nameIsGenericInDoc(parent.docComment, text)) {
                    return true
                }
            }

            parent = parent.parent
        }

        return false
    }

    private fun nameIsGenericInDoc(doc: PhpDocComment?, text: String): Boolean {
        if (doc == null) return false

        for (child in doc.children) {
            if (child is KphpDocTagGenericPsiImpl && child.getOnlyNameGenericArguments().contains(text)) {
                return true
            }
        }
        return false
    }
}
