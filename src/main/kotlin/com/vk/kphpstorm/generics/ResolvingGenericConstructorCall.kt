package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeForcing
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.typeProviders.GenericClassesTypeProvider

class ResolvingGenericConstructorCall(project: Project) : ResolvingGenericCallBase(project) {
    override fun instantiate(): PhpType {
        val specializationNameMap = specialization()

        val genericsTypes = genericTs.map { ExPhpTypeGenericsT(it.name) }
        val instantiationType = ExPhpTypeTplInstantiation(klass!!.fqn, genericsTypes)
        val specializedType = instantiationType.instantiateGeneric(specializationNameMap)

        return ExPhpTypeForcing(specializedType).toPhpType()
    }
    
    override fun unpack(packedData: String): Boolean {
        if (beginCompleted(packedData)) {
            val firstSeparator = packedData.indexOf(".__construct")
            if (firstSeparator != -1) {
                val className = packedData.substring(1, firstSeparator)
                klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull()
                if (klass?.isGeneric() == false)
                    return false
            }
        }

        val data = resolveSubTypes(packedData)
        val parts = safeSplit(data, 3, GenericClassesTypeProvider.SEP) ?: return false

        val fqn = parts[0]
        val explicitGenericsString = parts[1]
        val argumentsTypesString = parts[2]

        val className = fqn.split(".").first()

        if (klass == null) {
            klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull() ?: return false
        }

        val method = klass!!.constructor

        // Если у класса есть конструктор, получаем из него параметры, если нет, то считаем что параметров нет
        parameters = method?.parameters ?: emptyArray()
        genericTs = klass!!.genericNames()

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}
