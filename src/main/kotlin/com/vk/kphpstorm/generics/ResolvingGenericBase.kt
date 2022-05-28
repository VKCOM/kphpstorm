package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import org.apache.commons.lang3.tuple.MutablePair
import java.util.*

/**
 * Данный класс инкапсулирует логику обработки данных полученных на этапе
 * индексации и вывода типов ([IndexingGenericFunctionCall]).
 *
 * Результатом для данного класса являются данные возвращаемые методом
 * [specialization], данный метод возвращает список шаблонных типов
 * для данного вызова.
 */
abstract class ResolvingGenericBase(val project: Project) {
    abstract var parameters: Array<Parameter>
    abstract var genericTs: List<KphpDocGenericParameterDecl>

    protected lateinit var argumentsTypes: List<ExPhpType>
    protected lateinit var explicitGenericsT: List<ExPhpType>

    private val reifier = GenericsReifier(project)

    fun specialization(): List<ExPhpType> {
        return explicitGenericsT.ifEmpty { reifier.implicitSpecs }
    }

    fun unpack(packedData: String): Boolean {
        if (unpackImpl(packedData)) {
            reifier.reifyAllGenericsT(klass(), parameters, genericTs, argumentsTypes, null)
            return true
        }

        return false
    }

    abstract fun klass(): PhpClass?

    protected fun getAtLeast(data: String, count: Int, separator: String): List<String>? {
        var remainingData = data
        var countTaken = 0
        val parts = mutableListOf<String>()

        while (true) {
            val sepIndex = remainingData.indexOf(separator)
            if (sepIndex == -1) {
                parts.add(remainingData)
                countTaken++
                if (countTaken == count) {
                    return parts
                }
                return null
            }

            val part = remainingData.substring(0, sepIndex)
            parts.add(part)

            remainingData = remainingData.substring(sepIndex + separator.length)
            countTaken++
            if (countTaken == count) {
                parts.add(remainingData)
                break
            }
        }

        return if (parts.count() >= count) parts else null
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

    protected abstract fun unpackImpl(packedData: String): Boolean

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
