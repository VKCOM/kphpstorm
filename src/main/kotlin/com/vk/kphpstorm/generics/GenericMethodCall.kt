package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiations
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import kotlin.math.min

class GenericMethodCall(call: MethodReference) : GenericCall(call.project) {
    override val element = call
    override val arguments: Array<PsiElement> = call.parameters
    override val argumentTypes: List<ExPhpType?> = arguments
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }

    private val method = call.resolve() as? Method
    // TODO
    private val containingClass = method?.containingClass

    override val klass: PhpClass?

    init {
        val callType = call.classReference?.type?.global(project)

        val classType = PhpType().add(callType).global(project)
        val parsed = classType.toExPhpType()

        val instantiation = parsed?.getInstantiations()?.firstOrNull()

        if (instantiation != null) {
            klass = PhpIndex.getInstance(project).getAnyByFQN(instantiation.classFqn).firstOrNull()

            val specialization = instantiation.specializationList
            val classSpecializationNameMap = mutableMapOf<String, ExPhpType>()
            val genericNames = klass?.genericNames() ?: emptyList()

            for (i in 0 until min(genericNames.size, specialization.size)) {
                classSpecializationNameMap[genericNames[i].name] = specialization[i]
            }

            classSpecializationNameMap.forEach { (name, type) ->
                reifier.implicitClassSpecializationNameMap[name] = type
            }
        } else {
            klass = null
        }

        init()
    }

    fun isStatic() = method?.isStatic ?: false

    override fun function() = method

    override fun isResolved() = method != null && klass != null

    override fun genericNames(): List<KphpDocGenericParameterDecl> {
        val methodsNames = method?.genericNames() ?: emptyList()
        val classesNames = klass?.genericNames() ?: emptyList()

        return mutableListOf<KphpDocGenericParameterDecl>()
            .apply { addAll(methodsNames) }
            .apply { addAll(classesNames) }
            .toList()
    }

    override fun ownGenericNames() = method?.genericNames() ?: emptyList()

    override fun isGeneric() = genericNames().isNotEmpty()

    override fun toString(): String {
        val function = function()
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${klass?.fqn ?: "UnknownClass"}->${function?.name ?: "UnknownMethod"}<$explicit>($implicit)"
    }
}
