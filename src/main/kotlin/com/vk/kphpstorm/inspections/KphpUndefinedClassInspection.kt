package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.fileEditor.UniqueVFilePathBuilder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.inspections.quickfix.PhpImportClassQuickFix
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.inspections.helpers.KphpTypingAnalyzer

/**
 * Purpose:
 * In phpdoc types we want 'future', 'any' and other kphp-specific types be valid.
 * But native mechanisms think that 'any' is a ref to class and report "Unknown class" warning.
 *
 * To prevent this, a custom inspection "Undefined class" is written.
 * Important! Native inspection "PHP > Undefined > Undefined Class" must be disabled.
 * @see com.jetbrains.php.lang.inspections.PhpUndefinedClassInspection
 */
class KphpUndefinedClassInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            /**
             * On class declaration,
             * report if any other declaration with the same fqn exists in other files
             */
            override fun visitPhpClass(clazz: PhpClass) {
                val identifier = clazz.nameIdentifier ?: return
                val classes = PhpIndex.getInstance(clazz.project).getClassesByFQN(clazz.fqn)
                val another = classes.find {
                    // report only declaration in other files:
                    // if 2 equal classes in the same file, it's an error (not warning) and is reported natively
                    it != clazz && it.containingFile != clazz.containingFile
                } ?: return

                val relativePath = UniqueVFilePathBuilder.getInstance().getUniqueVirtualFilePath(clazz.project, another.containingFile.virtualFile)
                holder.registerProblem(identifier, "Another declaration of class '#ref' exists in $relativePath", ProblemHighlightType.WEAK_WARNING)
            }

            /**
             * On class reference in PHP code (after 'new', in return type hint, instanceof, use, etc),
             * report if this class is unknown
             */
            override fun visitPhpClassReference(classReference: ClassReference) {
                // for "ns\CN": nameNode is "CN", name is "CN"
                val nameNode = classReference.nameNode ?: return
                val hasNoNs = classReference.firstChild == nameNode
                // 'int', 'callable' — scalar type hints — are also ClassReference
                // but function f(int $a):bool is ok, new int is not
                val isPrimitive = hasNoNs && KphpTypingAnalyzer.isScalarTypeHint(classReference.name!!)

                if (isPrimitive) {
                    if (classReference.parent !is PhpTypeDeclaration)
                        holder.registerProblem(nameNode.psi, "Incorrect primitive type usage", ProblemHighlightType.ERROR)
                    return
                }

                // use A\B — this must be either a class or a start part of some namespace
                if (classReference.parent is PhpUse) {
                    val fqn = classReference.fqn
                    val project = classReference.project

                    if (PhpIndex.getInstance(project).getChildNamespacesByParentName(fqn + "\\").isNotEmpty())
                        return
                    if (PhpIndex.getInstance(project).getNamespacesByName(fqn).isNotEmpty())
                        return
                }

                val resolved = classReference.multiResolve(false)
                if (resolved.isEmpty())
                    reportUndefinedClassUsage(classReference)
            }

            /**
             * Inside phpdoc instance references,
             * report if this class is unknown (primitives 'int', 'mixed', etc have another psi impl)
             */
            override fun visitPhpDocType(type: PhpDocType) {
                if (type !is ExPhpTypeInstancePsiImpl || type.isKphpBuiltinClass())
                    return

                val resolved = type.multiResolve(false)
                if (resolved.isEmpty())
                    reportUndefinedClassUsage(type)
            }

            /**
             * Highlight an undefined class usage and provide a quickfix to import that class(es)
             */
            private fun reportUndefinedClassUsage(classReference: PhpReference) {
                // candidates to import:
                // * if none, just report an error, no fixes available
                // * if one, this is the default fix
                // * if many, a prompt can be shown, but only in UI (not in a batch mode)
                // here we use PhpImportClassQuickFix — from a native inspection, see PhpUndefinedClassInspection
                val candidates = PhpImportClassQuickFix.INSTANCE.getCandidates(classReference.project, classReference)
                val psi = classReference.nameNode!!.psi
                val importAvailable = candidates.isNotEmpty() && (isOnTheFly || candidates.size == 1)

                if (importAvailable)
                    holder.registerProblem(psi, "Undefined class '#ref'", ProblemHighlightType.ERROR, PhpImportClassQuickFix.INSTANCE)
                else
                    holder.registerProblem(psi, "Undefined class '#ref'", ProblemHighlightType.ERROR)
            }

        }
    }
}
