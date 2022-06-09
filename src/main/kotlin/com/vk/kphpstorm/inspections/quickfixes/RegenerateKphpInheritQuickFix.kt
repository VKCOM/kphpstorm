package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.generics.GenericUtil.genericNonDefaultNames
import com.vk.kphpstorm.generics.GenericUtil.genericParents
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder.addTag
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder.removeTagFromDocComment
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder.transformToMultiline
import com.vk.kphpstorm.kphptags.KphpInheritDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocTagInheritPsiImpl

class RegenerateKphpInheritQuickFix(
    private val element: SmartPsiElementPointer<PsiElement>,
    private val needKeepExistent: Boolean = false,
    private val text: String = "Regenerate @kphp-inherit tag"
) : LocalQuickFixAndIntentionActionOnPsiElement(element.element) {

    override fun getFamilyName() = "Regenerate @kphp-inherit tag"
    override fun getText() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, start: PsiElement, end: PsiElement) {
        val klass = element.element as? PhpClass ?: return
        val docComment = klass.docComment ?: PhpDocPsiBuilder.createDocComment(project, klass, "\n * @kphp-generic T\n")
        val genericTag = docComment.getTagElementsByName("@kphp-generic").lastOrNull()

        val newParents = if (needKeepExistent) {
            val containingNamespace = klass.namespaceName
            val inheritTag = docComment.getTagElementsByName("@kphp-inherit").firstOrNull() as? KphpDocTagInheritPsiImpl
                ?: return
            val tagParents = inheritTag.types().associateBy { it.className() }

            val (extendsList, implementsList) = klass.genericParents()
            val definedParent = extendsList + implementsList

            val newParents = mutableListOf<String>()
            definedParent.forEach {
                if (tagParents.containsKey(it.fqn)) {
                    newParents.add(tagParents[it.fqn]!!.text)
                } else {
                    val genericTs = it.genericNonDefaultNames().joinToString(", ") { "T" }
                    newParents.add(it.fqn + "<$genericTs>")
                }
            }

            newParents.joinToString(", ") {
                it.removePrefix(containingNamespace)
            }
        } else {
            val (extendsGenericList, implementsGenericList) = klass.genericParents()

            val parentsList = extendsGenericList + implementsGenericList

            parentsList.joinToString(", ") {
                val genericTs = it.genericNonDefaultNames().joinToString(", ") { "T" }
                it.name + "<$genericTs>"
            }
        }

        removeTagFromDocComment(docComment, "@kphp-inherit")
        docComment
            .transformToMultiline(project)
            .addTag(project, KphpInheritDocTag.nameWithAt, newParents, genericTag)
    }
}
