package com.vk.kphpstorm.completion

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl

/**
 * Контрибьютор который создает ссылки на классы внутри комментария
 * с описанием шаблонных типов при вызове функции.
 *
 * Подробнее в комментарии для [GenericInstantiationPsiCommentImpl].
 */
class KphpGenericsReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiComment(),
            PhpPsiReferenceProvider()
        )
    }

    class PhpPsiReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            if (element !is GenericInstantiationPsiCommentImpl) {
                return emptyArray()
            }

            val instances = element.extractInstances()

            return instances.map { (_, instance) ->
                instance.classes(element.project).map { klass ->
                    PhpElementReference(element, klass, instance.pos.shiftLeft(element.startOffset))
                }
            }.flatten().toTypedArray()
        }

        class PhpElementReference(element: PsiElement, result: PsiElement, private val myRange: TextRange? = null) :
            PsiReference {
            private val myElement: PsiElement
            private val myResult: PsiElement

            init {
                myElement = element
                myResult = result
            }

            override fun getElement() = myElement

            override fun getRangeInElement(): TextRange {
                if (myRange != null) {
                    return myRange
                }
                val startOffset = 1
                return TextRange(startOffset, myElement.textLength - 1)
            }

            override fun resolve() = myResult

            override fun getCanonicalText(): String =
                if (myResult is PhpNamedElement) myResult.fqn
                else myElement.parent.text

            override fun handleElementRename(newElementName: String): PsiElement {
                val text = element.text
                val start = text.slice(0 until rangeInElement.startOffset)
                val end = text.slice(rangeInElement.endOffset until text.length)

                val specText = start + newElementName + end

                val psi = PhpPsiElementFactory.createPhpPsiFromText(
                    element.project, FunctionReference::class.java, "f$specText();"
                )

                val comment = psi.firstChild.nextSibling

                return myElement.replace(comment)
            }

            override fun bindToElement(element: PsiElement): PsiElement {
                throw UnsupportedOperationException()
            }

            override fun isReferenceTo(element: PsiElement): Boolean {
                return myResult === element
            }

            override fun isSoft() = true
        }
    }
}

