package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import kotlin.math.min

class GenericMethodCall(private val call: MethodReference) : GenericCall(call.project) {
    override val callArgs: Array<PsiElement> = call.parameters
    override val argumentsTypes: List<ExPhpType?> = callArgs
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }
    override val explicitSpecsPsi = GenericUtil.findInstantiationComment(call)

    private val method = call.resolve() as? Method
    private val klass = method?.containingClass

    init {
        val callType = call.classReference?.type?.global(project)

        val classType = PhpType().add(callType).global(project)
        val parsed = classType.toExPhpType()

        val instantiation = parsed?.getInstantiation()

        if (instantiation != null) {
            val specialization = instantiation.specializationList
            val classSpecializationNameMap = mutableMapOf<String, ExPhpType>()
            val genericNames = klass?.genericNames() ?: emptyList()

            for (i in 0 until min(genericNames.size, specialization.size)) {
                classSpecializationNameMap[genericNames[i].name] = specialization[i]
            }

            classSpecializationNameMap.forEach { (name, type) ->
                reifier.implicitClassSpecializationNameMap[name] = type
            }
        }

        init()
    }

    override fun element() = call

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

    override fun ownGenericNames(): List<KphpDocGenericParameterDecl> {
        return method?.genericNames() ?: emptyList()
    }

    override fun isGeneric() = genericNames().isNotEmpty()

    override fun toString(): String {
        val function = function()
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${klass?.fqn ?: "UnknownClass"}->${function?.name ?: "UnknownMethod"}<$explicit>($implicit)"
    }
}
