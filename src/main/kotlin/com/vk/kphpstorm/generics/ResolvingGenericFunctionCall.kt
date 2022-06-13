package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeForcing
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.typeProviders.GenericFunctionsTypeProvider

class ResolvingGenericFunctionCall(project: Project) : ResolvingGenericCallBase(project) {
    private var function: Function? = null

    override fun instantiate(): PhpType? {
        val specializationNameMap = specialization()

        val returnTag = function!!.docComment?.returnTag ?: return null
        val exType = returnTag.type.toExPhpType(project) ?: return null
        val specializedType = exType.instantiateGeneric(specializationNameMap)

        return ExPhpTypeForcing(specializedType).toPhpType().add(specializedType.toPhpType())
    }

    override fun unpack(packedData: String): Boolean {
        if (beginCompleted(packedData)) {
            val firstSeparator = packedData.indexOf(GenericFunctionsTypeProvider.SEP)
            if (firstSeparator != -1) {
                val functionName = packedData.substring(1, firstSeparator)
                function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull()
                if (function?.isReturnGeneric() == false)
                    return false
            }
        }

        val data = resolveSubTypes(packedData)
        val parts = safeSplit(data, 3, GenericFunctionsTypeProvider.SEP) ?: return false
        val functionName = parts[0]
        val explicitGenericsString = parts[1]
        val argumentsTypesString = parts[2]

        if (function == null) {
            function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull() ?: return false
        }

        // Если возвращаемый тип функции не зависит от шаблонного типа,
        // то нет смысла как-то уточнять ее тип.
        if (!function!!.isReturnGeneric()) {
            return false
        }

        genericTs = function!!.genericNames()
        parameters = function!!.parameters

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}
