package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.typeProviders.GenericMethodsTypeProvider

class ResolvingGenericMethodCall(project: Project) : ResolvingGenericBase(project) {
    var klass: PhpClass? = null
    var method: Method? = null
    var classGenericType: ExPhpTypeTplInstantiation? = null
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>
    lateinit var classGenericTs: List<KphpDocGenericParameterDecl>

    override fun klass(): PhpClass? = klass

    override fun unpackImpl(packedData: String): Boolean {
        val data = resolveSubTypes(packedData)
        val parts = getAtLeast(data, 3, GenericMethodsTypeProvider.SEP)
        if (parts == null) {
            return false
        }

        val fqn = parts[0]

        val dotIndex = fqn.lastIndexOf('.')
        val classRawName = fqn.substring(0, dotIndex)
        val methodName = fqn.substring(dotIndex + 1)

        val classType = PhpType().add(classRawName).global(project)
        val parsed = classType.toExPhpType()
        val instantiation = parsed?.getInstantiation()

        val className = if (instantiation != null && instantiation.specializationList.first() !is ExPhpTypeGenericsT) {
            classGenericType = instantiation
            instantiation.classFqn
        } else {
            classRawName
        }

        klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull() ?: return false
        method = klass!!.findMethodByName(methodName)
        if (method == null) {
            return false
        }

        parameters = method!!.parameters
        genericTs = method!!.genericNames()

        // Не устанавливаем параметры класса, так как это статический вызов
        if (!method!!.isStatic) {
            classGenericTs = klass!!.genericNames()
        }

        explicitGenericsT = unpackTypeArray(parts[1])
        argumentsTypes = unpackTypeArray(parts[2])

        return true
    }
}
