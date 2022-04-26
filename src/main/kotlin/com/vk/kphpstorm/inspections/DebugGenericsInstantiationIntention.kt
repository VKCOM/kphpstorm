package com.vk.kphpstorm.inspections

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpFile
import com.vk.kphpstorm.exphptype.ExPhpTypeTuple
import com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing

class DebugGenericsInstantiationIntention : PsiElementBaseIntentionAction() {
    override fun getText() = "Debug generics instantiation"
    override fun getFamilyName() = "Debug generics instantiation"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return element is PsiComment && element.elementType == PhpTokenTypes.C_STYLE_COMMENT
                && element.text.let { it.length > 6 && it[2] == '<' && it[it.length - 3] == '>' }
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val outerFile = element.containingFile as PhpFile
        val injected = InjectedLanguageManager.getInstance(project).findInjectedElementAt(element.containingFile, element.startOffset+3)
        val docTagGenericsInstantiation = PsiTreeUtil.getParentOfType(injected, PhpDocTag::class.java)
        
        if (docTagGenericsInstantiation != null) {
            val specList = PhpTypeToExPhpTypeParsing.parse(docTagGenericsInstantiation.type) as? ExPhpTypeTuple
            specList?.items?.forEachIndexed { i, sub ->
                println("[$i] = ${sub.toHumanReadable(outerFile)} (${sub.javaClass.simpleName})")
            }
        }
    }
}
