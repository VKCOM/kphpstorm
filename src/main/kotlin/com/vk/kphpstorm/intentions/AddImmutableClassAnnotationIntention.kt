package com.vk.kphpstorm.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.findParentInFile
import com.jetbrains.php.lang.psi.elements.AssignmentExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder
import com.vk.kphpstorm.kphptags.KphpImmutableClassDocTag

class AddImmutableClassAnnotationIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Add @kphp-immutable-class"
    override fun getFamilyName(): String = getText()

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!element.isClassNameNode()) {
            return false
        }

        val klass = element.parent as PhpClass
        if (klass.isAbstract || klass.isInterface || klass.isTrait || klass.isAnonymous) {
            return false
        }

        val klassDocNode = klass.docComment

        // do not suggest if already present
        if (klassDocNode != null && KphpImmutableClassDocTag.existsInDocComment(klassDocNode)) {
            return false
        }

        return !isClassLocallyImmutable(klass)
    }

    /**
     * Simple local class mutability check. If there is any field mutation in class,
     * the class is mutable. The only exception is the class constructor
     */
    private fun isClassLocallyImmutable(klass: PhpClass): Boolean {
        val searchScope = LocalSearchScope(klass)
        for (field in klass.fields) {
            val hasAnyMutation = ReferencesSearch.search(field, searchScope).any { ref ->
                val element = ref.element

                isMutatingOp(element) && !isInClassConstructor(klass, element)
            }

            if (hasAnyMutation) {
                return true
            }
        }

        return false
    }

    private fun isMutatingOp(psiElement: PsiElement): Boolean {
        val parent = psiElement.parent

        return parent is AssignmentExpression && parent.variable == psiElement
    }

    private fun isInClassConstructor(klass: PhpClass, psiElement: PsiElement): Boolean {
        val classConstructor = klass.constructor
        return classConstructor != null && psiElement.findParentInFile { e -> e == classConstructor } != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val klass = element.parent as PhpClass
        PhpDocPsiBuilder.addTagToClass(klass, KphpImmutableClassDocTag)
    }

    private fun PsiElement.isClassNameNode(): Boolean {
        val klass = this.parent as? PhpClass ?: return false
        return klass.nameIdentifier == this
    }
}
