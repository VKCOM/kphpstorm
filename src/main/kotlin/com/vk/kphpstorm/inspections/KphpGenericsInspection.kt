package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.findParentOfType
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.jetbrains.rd.util.first
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTplInstantiationPsiImpl
import com.vk.kphpstorm.generics.GenericCall
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.generics.GenericUtil.isStringableStringUnion
import com.vk.kphpstorm.inspections.quickfixes.AddExplicitInstantiationCommentQuickFix
import com.vk.kphpstorm.inspections.quickfixes.RegenerateKphpInheritQuickFix
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagInheritPsiImpl

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

            override fun visitPhpClass(klass: PhpClass) {
                checkInheritTag(klass)
            }

            private fun checkInheritTag(klass: PhpClass) {
                val extendsList = klass.extendsList.referenceElements.mapNotNull { it to (it.resolve() as? PhpClass) }
                val implementsList =
                    klass.implementsList.referenceElements.mapNotNull { it to (it.resolve() as? PhpClass) }

                val allParents = extendsList + implementsList

                val extendsGenericList = extendsList.filter { it.second?.isGeneric() ?: false }
                val implementsGenericList = implementsList.filter { it.second?.isGeneric() ?: false }

                val allGenericParents = extendsGenericList + implementsGenericList

                val inheritTag = klass.docComment?.getTagElementsByName("@kphp-inherit")
                    ?.firstOrNull() as? KphpDocTagInheritPsiImpl
                if (inheritTag == null && allGenericParents.isNotEmpty()) {
                    holder.registerProblem(
                        klass.nameIdentifier ?: klass,
                        "Class extends or implements generic class/interface, please specify @kphp-inherit",
                        ProblemHighlightType.GENERIC_ERROR,
                        RegenerateKphpInheritQuickFix(
                            SmartPointerManager.getInstance(klass.project).createSmartPsiElementPointer(klass),
                            needKeepExistent = false,
                            "Generate @kphp-inherit tag"
                        )
                    )
                    return
                }

                val tagParents = inheritTag?.getParametersPsi()?.associateBy { it.decl().name } ?: emptyMap()
                allGenericParents.forEach { (ref, parentCLass) ->
                    if (parentCLass == null) return@forEach

                    if (!tagParents.containsKey(parentCLass.fqn)) {
                        holder.registerProblem(
                            ref.element,
                            "Class extends generic class/interface, but this class not specified in @kphp-inherit",
                            ProblemHighlightType.GENERIC_ERROR,
                            RegenerateKphpInheritQuickFix(
                                SmartPointerManager.getInstance(klass.project).createSmartPsiElementPointer(klass),
                                needKeepExistent = true,
                            )
                        )
                    }
                }

                tagParents.forEach { (name, decl) ->
                    val parent = allParents.find { it.second?.fqn == name }
                    if (parent == null && decl.decl().name != null) {
                        return holder.registerProblem(
                            decl,
                            "Class/interface $name not extended or implemented class/interface ${klass.name}",
                            ProblemHighlightType.GENERIC_ERROR,
                            RegenerateKphpInheritQuickFix(
                                SmartPointerManager.getInstance(klass.project).createSmartPsiElementPointer(klass),
                                needKeepExistent = true,
                            )
                        )
                    }

                    if (parent?.second?.isGeneric() == false) {
                        return holder.registerProblem(
                            decl,
                            "It is not necessary to specify not generic class/interface $name in @kphp-inherit",
                            ProblemHighlightType.GENERIC_ERROR,
                            RegenerateKphpInheritQuickFix(
                                SmartPointerManager.getInstance(klass.project).createSmartPsiElementPointer(klass),
                                needKeepExistent = true,
                            )
                        )
                    }
                }
            }

            override fun visitPhpDocType(type: PhpDocType) {
                checkPhpDocType(type)
            }

            override fun visitPhpDocTag(tag: PhpDocTag) {
                checkGenericTag(tag)
            }

            private fun checkGenericTag(tag: PhpDocTag) {
                if (tag !is KphpDocTagGenericPsiImpl) {
                    return
                }

                var wasDefault = false
                tag.getFullGenericParameters().forEach {
                    if (it.defaultType != null) {
                        wasDefault = true
                    }

                    if (it.defaultType == null && wasDefault) {
                        holder.registerProblem(
                            tag,
                            "Generic parameters with a default type cannot come before parameters without a default type",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }
            }

            private fun checkPhpDocType(type: PhpDocType) {
                // If it's ExPhpTypeInstancePsiImpl (Vector) in Vector<int>.
                if (type is ExPhpTypeInstancePsiImpl && type.parent is ExPhpTypeTplInstantiationPsiImpl) {
                    return
                }

                val instanceType = when (type) {
                    is ExPhpTypeInstancePsiImpl, is ExPhpTypeTplInstantiationPsiImpl -> type
                    else -> null
                } ?: return

                val inKphpGenericTag = instanceType.findParentOfType<KphpDocTagGenericPsiImpl>() != null

                val resolvedType = PhpTypeToExPhpTypeParsing.parse(instanceType.type) ?: return
                if (resolvedType is ExPhpTypeInstance && inKphpGenericTag) {
                    // Don't check instances in @kphp-generic tags.
                    return
                }

                val (className, countSpecs) = if (resolvedType is ExPhpTypeInstance) {
                    resolvedType.fqn to 0
                } else if (resolvedType.getInstantiation() != null) {
                    val instantiation = resolvedType.getInstantiation()!!
                    val countExplicitSpecs = instantiation.specializationList.size

                    instantiation.classFqn to countExplicitSpecs
                } else {
                    return
                }

                val klass = PhpIndex.getInstance(type.project).getAnyByFQN(className).firstOrNull() ?: return
                if (!klass.isGeneric()) {
                    return
                }

                val genericNames = klass.genericNames()

                reportParamsCountMismatch(genericNames, countSpecs, type)
            }

            private fun checkGenericCall(call: GenericCall, element: PsiElement, errorPsi: PsiElement) {
                if (!call.isResolved()) return
                val genericNames = call.genericNames()

                checkGenericTypesBounds(call, genericNames)
                checkInstantiationParamsCount(call)
                checkReifiedGenericTypes(call, element, errorPsi)
                checkReifiedSeveralGenericTypes(call, element, errorPsi)
            }

            private fun checkReifiedSeveralGenericTypes(call: GenericCall, element: PsiElement, errorPsi: PsiElement) {
                // В случае даже если есть ошибки, то мы показываем их только
                // в случае когда нет явного определения шаблона для вызова функции.
                if (call.implicitSpecializationErrors.isEmpty() || call.withExplicitSpecs()) return

                val error = call.implicitSpecializationErrors.first()
                val (type1, type2) = error.value

                holder.registerProblem(
                    errorPsi,
                    "Couldn't reify generic <${error.key}>: it's both $type1 and $type2",
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

            private fun checkInstantiationParamsCount(call: GenericCall) {
                val genericNames = call.ownGenericNames()

                val countExplicitSpecs = call.explicitSpecs.size
                val explicitSpecsPsi = call.explicitSpecsPsi

                reportParamsCountMismatch(genericNames, countExplicitSpecs, explicitSpecsPsi)
            }

            private fun reportParamsCountMismatch(
                genericNames: List<KphpDocGenericParameterDecl>,
                countSpecs: Int,
                errorPsi: PsiElement?
            ) {
                val minCount = genericNames.filter { it.defaultType == null }.size
                val maxCount = genericNames.size

                if (minCount == maxCount && minCount != countSpecs && errorPsi != null) {
                    holder.registerProblem(
                        errorPsi,
                        "$minCount generic parameters expected, but $countSpecs passed",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                    return
                }

                if (countSpecs < minCount && errorPsi != null) {
                    holder.registerProblem(
                        errorPsi,
                        "Not enough generic parameters, expected at least $minCount",
                        ProblemHighlightType.GENERIC_ERROR,
                    )
                    return
                }

                if (countSpecs > maxCount && errorPsi != null) {
                    holder.registerProblem(
                        errorPsi,
                        "Too many generic parameters, expected at most $maxCount",
                        ProblemHighlightType.GENERIC_ERROR,
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
                    if (resolvedType is ExPhpTypeGenericsT) return@forEach

                    val upperBoundType = decl.extendsType ?: return@forEach

                    val errorPsi =
                        call.explicitSpecsPsi
                            ?: call.arguments.firstOrNull()
                            ?: call.element

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
