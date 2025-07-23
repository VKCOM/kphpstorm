package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.psi.ExPhpTypePrimitivePsiImpl
import com.vk.kphpstorm.intentions.prettifier.findAllPhpDocPrettifications
import com.vk.kphpstorm.inspections.quickfixes.AddKphpSerializedFieldQuickFix
import com.vk.kphpstorm.inspections.quickfixes.PrettifySomethingInDocBlockQuickFix
import com.vk.kphpstorm.kphptags.KphpSerializableDocTag
import com.vk.kphpstorm.kphptags.KphpSerializedFieldDocTag

/**
 * Performs various phpdoc checks and simplification:
 * 1) remove @param/@return/@var duplicating type hint
 * 2) convert multiline @var to single-line
 * 3) convert @param $var {type} to @param {type} $var
 * 4) simplify phpdoc types (string|null → ?string, etc)
 * 5) @kphp-serialized-field is present for serializable classes
 *
 * Important! Native inspection "PHP > PHPDoc > PHPDoc comment matches function/method signature" must be disabled.
 * @see com.jetbrains.php.lang.inspections.phpdoc.PhpDocSignatureInspection
 */
class KphpDocInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            override fun visitElement(element: PsiElement) {
                if (element is PhpDocComment) {
                    findAllPhpDocPrettifications(element).forEach {
                        holder.registerProblem(it.getHighlightElement(), it.getDescriptionText(), it.getHightlightType(), PrettifySomethingInDocBlockQuickFix(it))
                    }
                }
            }

            override fun visitPhpField(field: Field) {
                val nameIdentifier = field.nameIdentifier ?: return
                val clazz = field.containingClass ?: return

                if (KphpSerializableDocTag.existsInDocComment(clazz) && !KphpSerializedFieldDocTag.existsInDocComment(field) && KphpSerializedFieldDocTag.isApplicableFor(field))
                    holder.registerProblem(nameIdentifier, "Field has no @kphp-serialized-field tag", ProblemHighlightType.GENERIC_ERROR, AddKphpSerializedFieldQuickFix())
            }

            override fun visitPhpDocType(type: PhpDocType) {
                // this is not prettification — just warning — as no quick fix can be auto-applied
                if (type is ExPhpTypePrimitivePsiImpl && type.text == "array")
                    holder.registerProblem(type, "Use 'T[]', not just 'array'")
            }

        }
    }
}
