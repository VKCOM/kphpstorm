package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import org.apache.commons.lang3.tuple.MutablePair
import java.util.*
import kotlin.math.min

/**
 * Данный класс инкапсулирует логику обработки данных полученных на этапе
 * индексации и вывода типов ([IndexingGenericFunctionCall]).
 *
 * Результатом для данного класса являются данные возвращаемые методом
 * [specializationList], данный метод возвращает список шаблонных типов
 * для данного вызова.
 */
abstract class ResolvingGenericBase(val project: Project) {
    protected abstract var parameters: Array<Parameter>
    protected abstract var genericTs: List<KphpDocGenericParameterDecl>
    protected abstract var klass: PhpClass?

    protected abstract var classGenericType: ExPhpTypeTplInstantiation?
    protected abstract var classGenericTs: List<KphpDocGenericParameterDecl>

    protected lateinit var argumentsTypes: List<ExPhpType>
    protected lateinit var explicitGenericsT: List<ExPhpType>

    private val reifier = GenericsReifier(project)

    protected abstract fun instantiate(): PhpType?
    protected abstract fun unpackImpl(packedData: String): Boolean

    fun resolve(incompleteType: String): PhpType? {
        if (!unpack(incompleteType)) return null
        return instantiate()
    }

    protected fun specialization(): Map<String, ExPhpType> {
        val specialization = specializationList()

        val specializationNameMap = mutableMapOf<String, ExPhpType>()

        for (i in 0 until min(genericTs.size, specialization.size)) {
            specializationNameMap[genericTs[i].name] = specialization[i]
        }

        if (classGenericType != null) {
            for (i in 0 until min(classGenericTs.size, classGenericType!!.specializationList.size)) {
                specializationNameMap[classGenericTs[i].name] = classGenericType!!.specializationList[i]
            }
        }

        return specializationNameMap
    }

    private fun explicitGenericTypes(): List<ExPhpType> {
        if (explicitGenericsT.isEmpty()) return emptyList()

        val specMap = mutableMapOf<String, ExPhpType>()

        genericTs.forEachIndexed { index, genericT ->
            val type = explicitGenericsT.getOrNull(index) ?: genericT.defaultType ?: return@forEachIndexed
            specMap[genericT.name] = type.instantiateGeneric(specMap)
        }

        return genericTs.mapNotNull { specMap[it.name] }
    }

    private fun specializationList() = explicitGenericTypes().ifEmpty { reifier.implicitSpecs }

    private fun unpack(incompleteType: String): Boolean {
        val packedData = incompleteType.substring(2)

        if (unpackImpl(packedData)) {
            reifier.reifyAllGenericsT(klass, parameters, genericTs, argumentsTypes, null)
            return true
        }

        return false
    }

    protected fun beginCompleted(packedData: String): Boolean {
        return packedData.startsWith(IndexingGenericFunctionCall.START_TYPE + "\\")
    }

    protected fun safeSplit(data: String, count: Int, separator: String): List<String>? {
        val parts = data.split(separator)
        if (parts.size != count) return null
        return parts
    }

    protected fun resolveSubTypes(packedData: String): String {
        var data = packedData
            .removePrefix(IndexingGenericFunctionCall.START_TYPE.toString())
            .removeSuffix(IndexingGenericFunctionCall.END_TYPE.toString())

        val pairsParts = mutableListOf<MutableList<MutablePair<Int, Int>>>()

        val pairStack = Stack<MutablePair<Int, Int>>()
        for (i in data.indices) {

            if (data[i] == IndexingGenericFunctionCall.START_TYPE) {
                if (pairsParts.isEmpty()) {
                    pairsParts.add(mutableListOf())
                }

                val pair = MutablePair(i - 2, -1)
                pairsParts.last().add(pair)

                pairStack.add(pair)
            }

            if (data[i] == IndexingGenericFunctionCall.END_TYPE) {
                if (pairStack.isNotEmpty()) {
                    val pair = pairStack.pop()
                    pair.right = i + 1
                } else {
                    print("")
                }

                if (pairStack.isEmpty()) {
                    pairsParts.add(mutableListOf())
                }
            }
        }

        pairsParts.reverse()
        pairsParts.forEach { pairs ->
            pairs.reverse()
            var rightShift = 0

            var prevPair: MutablePair<Int, Int>? = null
            for (pair in pairs) {
                val startIndex = pair.left
                val endIndex = pair.right - if (prevPair != null) {
                    if (pair.right < prevPair.left) {
                        0
                    } else {
                        rightShift
                    }
                } else {
                    0
                }

                val subType = try {
                    data.substring(startIndex, endIndex)
                } catch (e: IndexOutOfBoundsException) {
                    ""
                }

                val resolvedSubType = PhpType().add(subType).global(project).toExPhpType()?.toString() ?: ""

                rightShift += subType.length - resolvedSubType.length

                try {
                    data = data.replaceRange(startIndex until endIndex, resolvedSubType)
                } catch (e: Exception) {
                    print("")
                }

                prevPair = pair
            }
        }

        return data
    }

    protected fun unpackTypeArray(text: String) = if (text.isNotEmpty())
        text.split("$$").mapNotNull {
            val types = it.split("→")
            val type = PhpType()
            types.forEach { singleType ->
                type.add(singleType)
            }
            try {
                type.global(project).toExPhpType()
            } catch (e: Exception) {
                null
            }
        }
    else
        emptyList()
}
