package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpCharTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpTypeCallable
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.generics.ResolvingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericMethodCall
import com.vk.kphpstorm.helpers.toExPhpType

class CallableTypeProvider : PhpTypeProvider4 {
    companion object {
        const val SEP = "⊘"
        val KEY = PhpCharTypeKey('⊡')
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement): PhpType? {
        if (p is Parameter) {
            val callableFunction = p.parentOfType<Function>() ?: return null
            if (callableFunction.name.isNotEmpty()) return null

            val parameterList = p.parentOfType<ParameterList>() ?: return null
            val indexInCallable = parameterList.parameters.indexOfFirst { it == p }

            val functionCall = parameterList.parentOfType<FunctionReference>() ?: return null
            val indexInFunctionCall = functionCall.parameters.indexOfFirst { it.firstChild == callableFunction }

            val isMethod = functionCall is MethodReference

//            val data = IndexingGenericFunctionCall(
//                functionCall.fqn!!,
//                functionCall.parameters,
//                functionCall,
//                GenericFunctionsTypeProvider.SEP
//            ).pack()

            val key: Char
            val callTypes = if (isMethod) {
                key = GenericMethodsTypeProvider.KEY.key
                GenericMethodsTypeProvider().getType(functionCall)
            } else {
                key = GenericFunctionsTypeProvider.KEY.key
                GenericFunctionsTypeProvider().getType(functionCall)
            }

            val resultType = PhpType()

            callTypes?.types?.forEach { type ->
                val data = type.substring(2)
                resultType.add(
                    KEY.sign("$key$data$SEP$indexInFunctionCall$SEP$indexInCallable")
                )
            }

            return resultType
        }

        return null
    }

    override fun complete(incompleteType: String, project: Project): PhpType? {
        val key = incompleteType[2]
        val data = incompleteType.substring(3)
        if (data.split(SEP).size < 3) return null

        val (functionCall, indexInFunctionCall, indexInCallable) = data.split(SEP)

        val call = when (key) {
            GenericFunctionsTypeProvider.KEY.key -> {
                ResolvingGenericFunctionCall(project)
            }
            GenericMethodsTypeProvider.KEY.key -> {
                ResolvingGenericMethodCall(project)
            }
            else -> return null
        }
        val paramType = call.paramType("$$$functionCall", indexInFunctionCall.toInt())
        val paramExType = paramType?.toExPhpType() ?: return null

        val callableType = when (paramExType) {
            is ExPhpTypePipe -> paramExType.items.find { it is ExPhpTypeCallable } as? ExPhpTypeCallable
            is ExPhpTypeCallable -> paramExType
            is ExPhpTypeNullable -> paramExType.inner as? ExPhpTypeCallable
            else -> null
        } ?: return null

        val callableParamType = callableType.argTypes[indexInCallable.toInt()]

        return callableParamType.toPhpType()
    }

    override fun emptyResultIsComplete() = true

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
