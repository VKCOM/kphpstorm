package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl

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

    protected abstract fun unpackImpl(packedData: String): Boolean

    protected fun unpackTypeArray(text: String) = if (text.isNotEmpty())
        text.split("$$").mapNotNull {
            val types = it.split("→")
            val type = PhpType()
            types.forEach { singleType ->
                type.add(singleType)
            }
            type.global(project).toExPhpType()
        }
    else
        emptyList()
}
