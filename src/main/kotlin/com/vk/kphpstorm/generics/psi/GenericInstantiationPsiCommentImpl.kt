package com.vk.kphpstorm.generics.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocReturnTagImpl
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpReference
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.NewExpressionImpl
import com.jetbrains.php.lang.psi.elements.impl.PhpReferenceImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * Комментарий вида `/*<T[, T2, ...]>*/` который пишется в вызове
 * функции между именем функции и списком аргументов.
 *
 * Данный комментарий хранит список явных шаблонных типов разделенных
 * запятой.
 *
 * В комментарии могут быть любимые типы которые могут быть представлены
 * в phpdoc.
 *
 * Комментарий не имеет внутренней структуры, так как [PsiComment] не может
 * иметь потомков, так как является листом дерева. В случае если не делать
 * данный узел комментарием, то ломается парсинг вызова функции, так как
 * грамматика не готова к тому что между именем функции и списком аргументов
 * есть еще какой-то элемент.
 */
class GenericInstantiationPsiCommentImpl(type: IElementType, text: CharSequence) : PsiCommentImpl(type, text) {
    /**
     * For `/*<T>*/` is `<T>`
     */
    val genericSpecs = text.substring(2 until text.length - 2)

    fun instantiationPartsTypes(): List<ExPhpType> {
        val instantiationParts = instantiationParts()
        val instantiationTypes = instantiationParts.mapNotNull {
            PhpType().add(it.text).toExPhpType()
        }

        return instantiationTypes
    }

    private fun instantiationParts(): List<PsiElement> {
        val psi = PhpPsiElementFactory.createPsiFileFromText(
            project,
            "/** @return __ClassT$genericSpecs */class __ClassT {}"
        )

        val returnTag = PsiTreeUtil.findChildOfType(psi, PhpDocReturnTagImpl::class.java)!!
        val genericPsi = returnTag.lastChild?.prevSibling!!

        genericPsi.accept(object : PhpElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ExPhpTypeInstancePsiImpl && element.name != "__ClassT") {
                    val newName = resolveInstance(element.text)
                    val namePsi = element.firstChild
                    if (namePsi is LeafPsiElement) {
                        DebugUtil.performPsiModification<Exception>(null) {
                            namePsi.rawReplaceWithText(newName)
                        }
                    }
                }

                var child = element.firstChild
                while (child != null) {
                    child.accept(this)
                    child = child.nextSibling
                }
            }
        })

        val firstElement = genericPsi.firstChild?.nextSibling?.nextSibling
        val endElement = genericPsi.lastChild?.prevSibling
        var curElement: PsiElement? = firstElement ?: return emptyList()

        if (firstElement == endElement) {
            return listOf(endElement)
        }

        val genericSpecElements = mutableListOf<PsiElement>()

        while (curElement != endElement && curElement != null) {
            if (curElement is LeafPsiElement || curElement is PsiWhiteSpace) {
                curElement = curElement.nextSibling
                continue
            }

            genericSpecElements.add(curElement)
            curElement = curElement.nextSibling
        }

        if (endElement != null) {
            genericSpecElements.add(endElement)
        }

        return genericSpecElements
    }

    private fun resolveInstance(rawName: String): String {
        val (name, namespaceName) = splitAndResolveNamespace(this, rawName)

        val parent = prevSibling.parent
        val reference = if (parent is FunctionReference) {
            parent
        } else if (parent is NewExpressionImpl) {
            parent.classReference
        } else {
            null
        } ?: return namespaceName + name

        return resolveLocal(
            reference,
            name, namespaceName
        )
    }

    private fun resolveLocal(reference: PhpReference, name: String, namespaceName: String): String {
        val aClass: String
        val pluralisedType: String
        if (name == "parent") {
            aClass = "parent"
        } else {
            val elements = resolveLocalInner(reference, name, namespaceName)
            if (elements.isNotEmpty()) {
                val element = elements.iterator().next() as PhpNamedElement
                pluralisedType = element.type.toString()
                aClass = if (element is PhpClass && PhpDocTypeImpl.isPolymorphicClassReference(name, element))
                    PhpTypeSignatureKey.POLYMORPHIC_CLASS.sign(pluralisedType)
                else
                    pluralisedType
            } else {
                aClass = namespaceName + name
            }
        }

        val type = PhpType()
        if (aClass.length > 1) {
            type.add(aClass)
        }

        return aClass
    }

    private fun resolveLocalInner(
        element: PhpReference,
        name: String,
        namespaceName: String
    ): Set<PhpNamedElement?> {
        if ((PhpType.isPrimitiveType(name) || "\\callback".equals(
                PhpLangUtil.toFQN(name),
                ignoreCase = true
            )) && !PhpLangUtil.mayBeReferenceToUserDefinedClass(name, project)
        ) {
            return emptySet()
        }

        return ClassReferenceImpl.resolveLocal(element, name, namespaceName)
    }

    /**
     * Функция принимает текущий контекстный элемент и имя класса, которое
     * может быть как FQN, так и просто именем.
     *
     * В случае когда было передано FQN, она разделяет его на пространство
     * имен и имя класса и возвращает их.
     *
     * В другом случае она находит текущее пространство имен в котором
     * находится контекстный элемент и возвращает его вместе с переданным
     * именем.
     */
    private fun splitAndResolveNamespace(el: PsiElement, nameOrFqn: String): Pair<String, String> {
        // if fqn
        if (nameOrFqn.startsWith('\\')) {
            val lastSlash = nameOrFqn.lastIndexOf('\\')
            val ns = nameOrFqn.substring(0..lastSlash)
            val name = nameOrFqn.substring(lastSlash + 1)
            return Pair(name, ns)
        }

        return Pair(nameOrFqn, PhpReferenceImpl.findNamespaceName("", el))
    }
}
