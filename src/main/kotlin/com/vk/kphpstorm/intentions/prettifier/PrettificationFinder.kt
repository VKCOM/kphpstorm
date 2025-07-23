package com.vk.kphpstorm.intentions.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocVariable
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing
import com.vk.kphpstorm.exphptype.psi.ExPhpTypePipePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeShapePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTuplePsiImpl
import com.vk.kphpstorm.helpers.getOwnerSmart
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.inspections.helpers.KphpTypingAnalyzer
import com.vk.kphpstorm.kphptags.ALL_KPHPDOC_TAGS
import com.vk.kphpstorm.kphptags.KphpInferDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocTagImpl

fun findFirstPhpDocPrettification(docComment: PhpDocComment): PhpDocPrettification? {
    return findPhpDocPrettifications(docComment, stopOnFirst = true).firstOrNull()
}

fun findAllPhpDocPrettifications(docComment: PhpDocComment): List<PhpDocPrettification> {
    return findPhpDocPrettifications(docComment, stopOnFirst = false)
}


private fun findPhpDocPrettifications(docComment: PhpDocComment, stopOnFirst: Boolean): List<PhpDocPrettification> {
    val list = mutableListOf<PhpDocPrettification>()
    val project = docComment.project

    // when invoked on docComment, this visitor visit only its children (tags, types, etc)
    // it does NOT visit function/method/field it is attached to
    // so, in order to analyze function/method, these methods are called explicitly, see below
    val visitor = object : PhpElementVisitor() {

        override fun visitElement(element: PsiElement) {
            if (stopOnFirst && list.isNotEmpty())
                return
            element.acceptChildren(this)
        }

        override fun visitPhpMethod(method: Method) {
        }

        override fun visitPhpFunction(function: Function) {
        }

        override fun visitPhpDocType(type: PhpDocType) {
            super.visitPhpDocType(type)

            if (type is ExPhpTypeTuplePsiImpl && type.prevSibling != null && type.prevSibling.elementType == PhpDocTokenTypes.DOC_NAMESPACE)
                list.add(LeadingSlashPrettification(type))

            if (type is ExPhpTypeShapePsiImpl && type.prevSibling != null && type.prevSibling.elementType == PhpDocTokenTypes.DOC_NAMESPACE)
                list.add(LeadingSlashPrettification(type))

            if (type is ExPhpTypePipePsiImpl) {
                val phpTypes = type.type.types
                val replacementPart = when {
                    phpTypes.size == 2 && phpTypes.first() == "\\null" -> phpTypes.last()
                    phpTypes.size == 2 && phpTypes.last() == "\\null"  -> phpTypes.first()
                    else                                               -> return
                }
                val replacementStr = "?" + PhpTypeToExPhpTypeParsing.parseFromString(replacementPart)?.toHumanReadable(type)
                list.add(NullableTypePrettification(type, replacementStr))
            }
        }

        override fun visitPhpDocTag(tag: PhpDocTag) {
            super.visitPhpDocTag(tag)

            if (tag is KphpDocTagImpl && tag.firstChild != null) {
                val nameWithAt = tag.firstChild!!.text
                val owner = tag.parentDocComment!!.getOwnerSmart()

                ALL_KPHPDOC_TAGS.find { it.nameWithAt == nameWithAt }.apply {
                    if (this != null && owner != null && !isApplicableFor(owner))
                        list.add(RemoveDocTagPrettification(tag, "Tag is not applicable here", "Remove strange $nameWithAt", ProblemHighlightType.WARNING))
                    if (this === KphpInferDocTag && !this.isKphpInferCast(tag))
                        list.add(RemoveDocTagPrettification(tag, "@kphp-infer is deprecated", "Remove useless @kphp-infer", ProblemHighlightType.WARNING))
                }
            }
            else if (tag is PhpDocParamTag) {
                val function = tag.parentDocComment?.getOwnerSmart() as? Function
                if (function != null)
                    visitPhpDocParamTag(function, tag)
                else if (tag.name == "@var")       // PhpStorm stores @var as PhpDocParamTag :)
                    visitPhpDocVarTag(tag.parentDocComment?.getOwnerSmart() as? Field, tag)
                checkTypeAndVarNameCorrectOrder(tag)
            }
            else if (tag is PhpDocReturnTag) {
                val function = tag.parentDocComment?.getOwnerSmart() as? Function ?: return
                visitPhpDocReturnTag(function, tag)
            }
        }

        private fun visitPhpDocParamTag(function: Function, tag: PhpDocParamTag) {
            val varName = tag.varName
            val fArg = function.parameters.find { it.name == varName }
            if (fArg == null) {
                list.add(RemoveDocTagPrettification(tag, "@param tag for unexisting argument", "Remove strange @param", ProblemHighlightType.GENERIC_ERROR))
                return
            }

            val hintType = fArg.declaredType.takeIf { !it.isEmpty }?.toExPhpType(project)
            val docType = tag.type.toExPhpType(project)

            // if both @param and type hint, check that they coincide
            if (hintType != null && docType != null) {
                if (!KphpTypingAnalyzer.doesDocTypeMatchTypeHint(docType, hintType, project))
                    list.add(RemoveDocTagPrettification(tag, "@param mismatches type hint", "Remove strange @param", ProblemHighlightType.GENERIC_ERROR))
                else if (KphpTypingAnalyzer.doesDocTypeDuplicateTypeHint(docType, hintType) && tag.tagValue.isBlank())
                    list.add(RemoveDocTagPrettification(tag, "@param just duplicates type hint", "Remove @param duplicating type hint"))
            }
            // if only @param, maybe it can be moved to type hint; vararg params are too complex to analyze, skip
            else if (hintType == null && docType != null && !fArg.isVariadic) {
                if (KphpTypingAnalyzer.isFunctionStrictlyTyped(function) && KphpTypingAnalyzer.canMoveToTypeHint(docType)) {
                    if (tag.tagValue.isBlank())
                        list.add(MoveParamTagToTypeHintPrettification(tag, docType.toHumanReadable(tag)))
                }
            }
        }

        private fun visitPhpDocReturnTag(function: Function, tag: PhpDocReturnTag) {
            val hintType = function.typeDeclaration?.type?.toExPhpType(project)
            val docType = tag.type.toExPhpType(project)

            if (docType === ExPhpType.VOID) {
                list.add(RemoveDocTagPrettification(tag, "@return void can be deleted", "Remove useless @return void"))
            }
            else if (hintType != null && docType != null) {
                if (!KphpTypingAnalyzer.doesDocTypeMatchTypeHint(docType, hintType, project))
                    list.add(RemoveDocTagPrettification(tag, "@return mismatches type hint", "Remove strange @return", ProblemHighlightType.GENERIC_ERROR))
                else if (KphpTypingAnalyzer.doesDocTypeDuplicateTypeHint(docType, hintType) && tag.tagValue.isBlank())
                    list.add(RemoveDocTagPrettification(tag, "@return just duplicates type hint", "Remove @return duplicating type hint"))
            }
            else if (hintType == null && docType != null) {
                if (KphpTypingAnalyzer.isFunctionStrictlyTyped(function) && KphpTypingAnalyzer.canMoveToTypeHint(docType)) {
                    if (tag.tagValue.isBlank())
                        list.add(MoveReturnTagToTypeHintPrettification(tag, docType.toHumanReadable(tag)))
                }
            }
        }

        private fun visitPhpDocVarTag(field: Field?, tag: PhpDocParamTag) {
            @Suppress("NAME_SHADOWING")
            val docComment = tag.parentDocComment!!
            if (docComment.firstPsiChild === tag
                    && PhpPsiUtil.getNextSiblingIgnoreWhitespace(docComment.firstChild, true) === PhpPsiUtil.getPrevSiblingIgnoreWhitespace(tag, true)
                    && PhpPsiUtil.getNextSiblingIgnoreWhitespace(tag, true).elementType == PhpDocTokenTypes.DOC_COMMENT_END) {
                val isMultiline = docComment.text.contains('\n')
                if (isMultiline)
                    list.add(ConvertVarToSingleLinePrettification(docComment))
            }
            if (field != null) {
                val hintType = field.typeDeclaration?.type?.toExPhpType(project)
                val docType = tag.type.toExPhpType(project)

                if (hintType != null && docType != null) {
                    if (!KphpTypingAnalyzer.doesDocTypeMatchTypeHint(docType, hintType, project))
                        list.add(RemoveDocTagPrettification(tag, "@var mismatches type hint", "Remove strange @var", ProblemHighlightType.GENERIC_ERROR))
                    else if (KphpTypingAnalyzer.doesDocTypeDuplicateTypeHint(docType, hintType) && tag.tagValue.isBlank())
                        list.add(RemoveDocTagPrettification(tag, "@var just duplicates type hint", "Remove @var duplicating type hint"))
                }
                else if (hintType == null && docType != null) {
                    if (KphpTypingAnalyzer.canMoveToTypeHint(docType) && tag.tagValue.isBlank())
                        list.add(MoveVarTagToFieldHintPrettification(tag, docType.toHumanReadable(tag)))
                }
            }
        }

        private fun checkTypeAndVarNameCorrectOrder(tag: PhpDocParamTag) {
            val first = tag.firstPsiChild ?: return
            val next = first.nextPsiSibling ?: return

            if (first is PhpDocVariable && next is PhpDocType)
                list.add(SwapTypeAndVarNamePrettification(tag))
        }

    }

    docComment.accept(visitor)
    when (val owner = docComment.getOwnerSmart()) {
        is Method   -> visitor.visitPhpMethod(owner)
        is Function -> visitor.visitPhpFunction(owner)
    }

    return list
}

