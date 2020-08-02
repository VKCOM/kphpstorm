package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement

interface PhpDocPrettification {
    fun getActionText(): String
    fun getDescriptionText(): String
    fun getHighlightElement(): PsiElement
    fun getHightlightType(): ProblemHighlightType

    fun applyPrettification()
}
