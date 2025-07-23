package com.vk.kphpstorm.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.PhpModifierList
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.intentions.prettifier.findFirstPhpDocPrettification

class PrettifyPhpdocBlockIntention : PsiElementBaseIntentionAction() {

    override fun getText() = "Prettify phpdoc block"
    override fun getFamilyName() = "Prettify phpdoc block"

    // where is intention available?
    // 1) inside phpdoc block
    // 2) inside owner of phpdoc block
    private fun getTargetDocComment(element: PsiElement): PhpDocComment? {
        val docComment = element.parentDocComment
        if (docComment != null)
            return docComment

        val parent = when (val p = element.parent) {
            is PhpModifierList -> p.parent
            else               -> p
        }
        return if (parent is PhpNamedElement) parent.docComment else null
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val docComment = getTargetDocComment(element) ?: return false
        return findFirstPhpDocPrettification(docComment) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        // when invoking "prettify doc block", do not use findAllPhpDocPrettifications()
        // because applying one of them can change presence of others
        // instead, find first while it can be found
        val docComment = getTargetDocComment(element) ?: return
        do {
            if (!docComment.isValid) break
            val prettification = findFirstPhpDocPrettification(docComment) ?: break
            prettification.applyPrettification()
        } while (true)
    }
}
