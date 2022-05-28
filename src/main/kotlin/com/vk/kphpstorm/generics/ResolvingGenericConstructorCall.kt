package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.typeProviders.GenericClassesTypeProvider

class ResolvingGenericConstructorCall(project: Project) : ResolvingGenericBase(project) {
    lateinit var klass: PhpClass
    var method: Method? = null
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>

    override fun klass(): PhpClass = klass

    override fun unpackImpl(packedData: String): Boolean {
        val data = resolveSubTypes(packedData)
        val parts = getAtLeast(data, 3, GenericClassesTypeProvider.SEP) ?: return false
        val fqn = parts[0]
        val explicitGenericsString = parts[1]
        val argumentsTypesString = parts[2]

        val className = fqn.split(".").first()

        klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull() ?: return false
        method = klass.constructor

        // Если у класса есть конструктор, получаем из него параметры, если нет, то считаем что параметров нет
        parameters = if (method != null) method!!.parameters else emptyArray()
        genericTs = klass.genericNames()

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentsTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}
