package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl

class ResolvingGenericFunctionCall(project: Project) : ResolvingGenericBase(project) {
    lateinit var function: Function
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>

    override fun klass(): PhpClass? = null

    override fun unpackImpl(packedData: String): Boolean {
        val data = resolveSubTypes(packedData)
        val parts = getAtLeast(data, 3, "@@")
        if (parts == null) {
            return false
        }
        val functionName = parts[0]
        val explicitGenericsString = parts[1]
        val argumentsTypesString = parts[2]

        function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull() ?: return false

        genericTs = function.genericNames()
        parameters = function.parameters

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentsTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}
