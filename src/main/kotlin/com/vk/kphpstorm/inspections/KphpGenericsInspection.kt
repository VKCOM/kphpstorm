package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpUse
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.jetbrains.rd.util.first
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeInstance
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.ExPhpTypePrimitive
import com.vk.kphpstorm.generics.GenericCall
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall
import com.vk.kphpstorm.generics.GenericUtil.isStringableStringUnion
import com.vk.kphpstorm.inspections.quickfixes.AddExplicitInstantiationCommentQuickFix
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

class KphpGenericsInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpNewExpression(expression: NewExpression) {
                val call = GenericConstructorCall(expression)
                checkGenericCall(call, expression, expression)
            }

            override fun visitPhpMethodReference(reference: MethodReference) {
                val call = GenericMethodCall(reference)
                checkGenericCall(call, reference, reference.firstChild.nextSibling.nextSibling)
            }

            override fun visitPhpFunctionCall(reference: FunctionReference) {
                if (reference.parent is PhpUse) {
                    return
                }
                val call = GenericFunctionCall(reference)
                checkGenericCall(call, reference, reference.firstChild)
            }

            override fun visitPhpDocTag(tag: PhpDocTag) {
                checkGenericsTag(tag)
            }

            private fun checkGenericsTag(tag: PhpDocTag) {
                if (tag !is KphpDocTagGenericPsiImpl) {
                    return
                }

                var wasNoDefault = false
                tag.getGenericArgumentsWithExtends().forEach {
                    if (it.defaultType == null) {
                        wasNoDefault = true
                    }

                    if (it.defaultType != null && wasNoDefault) {
                        holder.registerProblem(
                            tag,
                            "Generic parameters with a default type cannot come after parameters without a default type",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }
            }

            private fun checkGenericCall(call: GenericCall, element: PsiElement, errorPsi: PsiElement) {
                if (!call.isResolved()) return
                val genericNames = call.genericNames()

                checkGenericTypesBounds(call, genericNames)
                checkInstantiationArgsCount(call)
                checkReifiedGenericTypes(call, element, errorPsi)
                checkReifiedSeveralGenericTypes(call, element)
            }

            private fun checkReifiedSeveralGenericTypes(call: GenericCall, element: PsiElement) {
                // В случае даже если есть ошибки, то мы показываем их только
                // в случае когда нет явного определения шаблона для вызова функции.
                if (call.implicitSpecializationErrors.isEmpty() || call.withExplicitSpecs()) return

                val error = call.implicitSpecializationErrors.first()
                val (type1, type2) = error.value

                holder.registerProblem(
                    element,
                    "Couldn't reify generic <${error.key}> for call: it's both $type1 and $type2",
                    ProblemHighlightType.GENERIC_ERROR,
                    AddExplicitInstantiationCommentQuickFix(element),
                )
            }

            private fun checkReifiedGenericTypes(
                call: GenericCall,
                element: PsiElement,
                errorPsi: PsiElement
            ) {
                val decl = call.isNotEnoughInformation()
                if (decl != null) {
                    holder.registerProblem(
                        errorPsi,
                        "Not enough information to infer generic ${decl.name}",
                        ProblemHighlightType.GENERIC_ERROR,
                        AddExplicitInstantiationCommentQuickFix(element)
                    )
                }
            }

            private fun checkInstantiationArgsCount(call: GenericCall) {
                val countGenericNames = if (call is GenericMethodCall && call.isStatic()) {
                    call.ownGenericNames().size
                } else {
                    call.genericNames().size - call.implicitClassSpecializationNameMap.size
                }

                val countExplicitSpecs = call.explicitSpecs.size
                val explicitSpecsPsi = call.explicitSpecsPsi

                if (countGenericNames != countExplicitSpecs && explicitSpecsPsi != null) {
                    holder.registerProblem(
                        explicitSpecsPsi,
                        "$countGenericNames type arguments expected for ${call.function()!!.fqn}",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }

            private fun checkGenericTypesBounds(
                call: GenericCall,
                genericNames: List<KphpDocGenericParameterDecl>,
            ) {
                genericNames.forEach { decl ->
                    val (resolvedType, isExplicit) = if (call.specializationNameMap[decl.name] != null) {
                        call.specializationNameMap[decl.name] to true
                    } else {
                        call.implicitSpecializationNameMap[decl.name] to false
                    }

                    if (resolvedType == null) return@forEach

                    val upperBoundType = decl.extendsType ?: return@forEach

                    val errorPsi =
                        call.explicitSpecsPsi
                            ?: call.callArgs.firstOrNull()
                            ?: call.element()

                    if (!upperBoundType.isAssignableFrom(resolvedType, call.project)) {
                        val violationMessage = generateViolationMessage(call.project, upperBoundType, resolvedType)
                        val message =
                            "${if (isExplicit) "Explicit" else "Reified"} generic type for ${decl.name} is not within its bounds ($violationMessage)"

                        holder.registerProblem(
                            errorPsi,
                            message,
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }
            }

            private fun generateViolationMessage(
                project: Project,
                upperBoundType: ExPhpType,
                resolvedType: ExPhpType
            ): String {
                return when (upperBoundType) {
                    is ExPhpTypeInstance -> {
                        val klass = PhpIndex.getInstance(project).getAnyByFQN(upperBoundType.fqn).firstOrNull()
                        val extendsOrImplements = if (klass != null && klass.isInterface) "implement" else "extend"
                        "$resolvedType is not $extendsOrImplements ${upperBoundType.fqn}"
                    }
                    is ExPhpTypePrimitive -> {
                        "$resolvedType is not ${upperBoundType.typeStr})"
                    }
                    is ExPhpTypePipe -> {
                        val allInstance = upperBoundType.items.all { it is ExPhpTypeInstance }
                        val allPrimitives = upperBoundType.items.all { it is ExPhpTypePrimitive }

                        if (allInstance) {
                            val itemsString = upperBoundType.items.joinToString(" or ") { it.toString() }
                            "$resolvedType is none extend/implement any of $itemsString"
                        } else if (allPrimitives) {
                            val itemsString = upperBoundType.items.joinToString(" nor ") { it.toString() }
                            "$resolvedType is neither $itemsString"
                        } else if (upperBoundType.isStringableStringUnion()) {
                            "$resolvedType is not implement a \\Stringable and not a string"
                        } else {
                            ""
                        }
                    }
                    else -> {
                        ""
                    }
                }
            }
        }
    }
}
