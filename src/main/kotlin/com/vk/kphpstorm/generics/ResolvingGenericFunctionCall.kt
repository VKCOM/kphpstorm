package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeForcing
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.typeProviders.GenericFunctionsTypeProvider

class ResolvingGenericFunctionCall(project: Project) : ResolvingGenericBase(project) {
    private var function: Function? = null

    override var klass: PhpClass? = null
    override var classGenericType: ExPhpTypeTplInstantiation? = null
    override var classGenericTs: List<KphpDocGenericParameterDecl> = emptyList()

    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>

    override fun instantiate(): PhpType? {
        val specializationNameMap = specialization()

        val returnTag = function!!.docComment?.returnTag ?: return null
        val exType = returnTag.type.toExPhpType() ?: return null
        val specializedType = exType.instantiateGeneric(specializationNameMap)

        return ExPhpTypeForcing(specializedType).toPhpType().add(specializedType.toPhpType())
    }

    override fun unpackImpl(packedData: String): Boolean {
        if (beginCompleted(packedData)) {
            // Так как 99.99% функций не шаблонные, то мы должны как можно быстрее понять это
            // и не делать сложные вычисления. Поэтому получаем имя функции и проверяем что вывод
            // типов для нее действительно нужен.
            val firstSeparator = packedData.indexOf(GenericFunctionsTypeProvider.SEP)
            val functionName = packedData.substring(1, firstSeparator)
            function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull()
            if (function?.isReturnGeneric() == false)
                return false
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
        argumentsTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}
