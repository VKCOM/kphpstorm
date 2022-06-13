package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.setSelectionInEditor

class AddExplicitInstantiationCommentQuickFix(element: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
    override fun getFamilyName() = "Add explicit generic instantiation tag"
    override fun getText() = "Add explicit generic instantiation tag"

    override fun invoke(
        project: Project,
        file: PsiFile,
        editor: Editor?,
        startElement: PsiElement,
        endElement: PsiElement
    ) {
        val call = when (startElement) {
            is NewExpression -> GenericConstructorCall(startElement)
            is MethodReference -> GenericMethodCall(startElement)
            is FunctionReference -> GenericFunctionCall(startElement)
            else -> return
        }

        if (!call.isResolved()) {
            return
        }

        val genericTs = call.ownGenericNames()
        val genericTsString = genericTs.joinToString(", ") { it.name }
        val comment =
            PhpPsiElementFactory.createFromText(project, GenericInstantiationPsiCommentImpl::class.java, "/*<$genericTsString>*/")
                ?: return

        val insertedComment = when (startElement) {
            is NewExpression -> {
                val text = startElement.text
                if (!text.endsWith(")")) {
                    val newNewExpr =
                        PhpPsiElementFactory.createFromText(project, NewExpression::class.java, "$text/*<$genericTsString>*/()")
                            ?: return
                    startElement.replace(newNewExpr)
                    null
                } else {
                    startElement.addAfter(comment, startElement.classReference)
                }
            }
            is MethodReference -> {
                val after = startElement.firstChild?.nextSibling?.nextSibling ?: return
                startElement.addAfter(comment, after)
            }
            is FunctionReference -> {
                startElement.addAfter(comment, startElement.firstChild)
            }
            else -> return
        }

        if (editor != null && insertedComment != null) {
            setSelectionInEditor(editor, insertedComment, 3, 3 + genericTs.first().name.length)
        }
    }
}
