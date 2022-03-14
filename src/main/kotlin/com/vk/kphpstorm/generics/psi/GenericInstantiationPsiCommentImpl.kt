package com.vk.kphpstorm.generics.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocReturnTagImpl
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpReference
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.PhpReferenceImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.completion.KphpGenericsReferenceContributor
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

    class Instance(val fqn: String, val pos: TextRange) {
        fun classes(project: Project): List<PhpClass> {
            return PhpIndex.getInstance(project).getClassesByFQN(fqn).toList()
        }
    }

    /**
     * Предпосылки:
     *
     * Ввиду того, что комментарий не имеет структуры причины чего описаны в
     * комментарии над классом, нам необходимо по-другому вычленять составные части.
     *
     * Так как в комментарии описываются типы, то там могут быть и классы
     * для которых должно быть доступны переход к определению, переименование, поиск
     * использований и т.д.
     *
     * Для этого мы добавляем в комментарий ссылки (см. [KphpGenericsReferenceContributor]).
     * Для того чтобы смочь добавить эти ссылки, нам нужно для начала вычленить их
     * из комментария.
     *
     *
     * Описание:
     *
     * Данная функция находит все классы описанные в комментарии.
     * Для каждого класса она:
     * 1. Резолвит их имена в текущем контексте
     * 2. Находит их стартовую и конечную позицию
     *
     * Функция возвращает мапу где ключом является имя класса из комментария,
     * а значением список классов на которые ссылается это имя.
     *
     * Однако в комментарии один класс может появляться несколько раз, например:
     *
     *    <Foo, tuple(Boo, Foo)>
     *
     *
     * Поэтому в результирующей мапе ключом является не просто имя класса
     * как в комментарии, а имя + порядковый номер в комментарии.
     *
     * Таким образом в результате для примера выше мы получим следующую мапу:
     *
     *    Foo0 -> [...]
     *    Boo1 -> [...]
     *    Foo2 -> [...]
     */
    fun extractInstances(): Map<String, Instance> {
        val result = mutableMapOf<String, Instance>()

        val psi = PhpPsiElementFactory.createPsiFileFromText(
            project,
            "/** @return __ClassT$genericSpecs */class __ClassT {}"
        )

        val returnTag = PsiTreeUtil.findChildOfType(psi, PhpDocReturnTagImpl::class.java)!!

        val pseudoReturnTag = returnTag.firstChild
        val startOfTemplate = pseudoReturnTag.startOffset + "@return __ClassT".length + 1
        val genericPsi = returnTag.lastChild?.prevSibling!!

        val startInRealCode = startOffset + 3
        var instanceIndex = 0
        genericPsi.accept(object : PhpElementVisitor() {
            private fun resolveInstanceByName(name: String, startOffset: Int, endOffset: Int) {
                val fqn = resolveInstance(name)
                result["$name$instanceIndex"] = Instance(
                    fqn,
                    TextRange(startOffset, endOffset)
                )
                instanceIndex++
            }

            override fun visitElement(element: PsiElement) {
                val startOffset = startInRealCode + (element.startOffset - startOfTemplate)
                val endOffset = startInRealCode + (element.endOffset - startOfTemplate)

                if (element is ExPhpTypeInstancePsiImpl && element.name != "__ClassT") {
                    resolveInstanceByName(element.text, startOffset, endOffset)
                }

                var child = element.firstChild
                while (child != null) {
                    child.accept(this)
                    child = child.nextSibling
                }
            }
        })

        return result
    }

    fun instantiationParts(): List<PsiElement> {
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
                        namePsi.rawReplaceWithText(newName)
                    }
                }

                var child = element.firstChild
                while (child != null) {
                    child.accept(this)
                    child = child.nextSibling
                }
            }
        })

        val firstElement = genericPsi.firstChild.nextSibling.nextSibling
        val endElement = genericPsi.lastChild.prevSibling
        var curElement = firstElement

        if (firstElement == endElement) {
            return listOf(endElement)
        }

        val genericSpecElements = mutableListOf<PsiElement>()

        while (curElement != endElement) {
            if (curElement is LeafPsiElement || curElement is PsiWhiteSpace) {
                curElement = curElement.nextSibling
                continue
            }

            genericSpecElements.add(curElement)
            curElement = curElement.nextSibling
        }

        genericSpecElements.add(endElement)

        return genericSpecElements
    }

    fun instantiationPartsTypes(): List<ExPhpType> {
        val instantiationParts = instantiationParts()
        val instantiationTypes = instantiationParts.map {
            PhpType().add(it.text).toExPhpType()!!
        }

        return instantiationTypes
    }


    private fun resolveInstance(rawName: String): String {
        val (name, namespaceName) = splitAndResolveNamespace(this, rawName)

        return resolveLocal(
            prevSibling.parent as FunctionReference,
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

    // TODO: удалить
    private fun resolveGlobal(element: PhpReference, name: String, namespaceName: String): Collection<PhpNamedElement> {
        return ClassReferenceImpl.resolveGlobal(element, name, namespaceName, false)
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
