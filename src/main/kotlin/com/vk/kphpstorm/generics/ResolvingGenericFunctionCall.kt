package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl

class ResolvingGenericFunctionCall(project: Project) : ResolvingGenericBase(project) {
    var function: Function? = null
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>

    override fun klass(): PhpClass? = null

    override fun unpackImpl(packedData: String): Boolean {
        if (packedData.startsWith(IndexingGenericFunctionCall.START_TYPE + "\\")) {
            // Так как 99.99% функций не шаблонные, то мы должны как можно быстрее понять это
            // и не делать сложные вычисления. Поэтому получаем имя функции и проверяем что вывод
            // типов для нее действительно нужен.
            val firstSeparator = packedData.indexOf("@@")
            val functionName = packedData.substring(1, firstSeparator)
            function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull()
            if (function?.isReturnGeneric() == false)
                return false
        }

        val data = resolveSubTypes(packedData)
        val parts = getAtLeast(data, 3, "@@")
        if (parts == null) {
            return false
        }
        val functionName = parts[0]
        val explicitGenericsString = parts[1]
        val argumentsTypesString = parts[2]

        if (function == null) {
            function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull() ?: return false
        }

        genericTs = function!!.genericNames()
        parameters = function!!.parameters

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentsTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}
