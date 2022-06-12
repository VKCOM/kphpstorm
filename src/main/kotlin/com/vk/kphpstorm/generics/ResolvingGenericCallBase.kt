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
 * Responsible for processing data from [IndexingGenericCall.pack].
 *
 * Override [instantiate] and [unpack] methods for each case you need.
 *
 * @see IndexingGenericCall
 */
abstract class ResolvingGenericCallBase(protected val project: Project) {
    protected var parameters: Array<Parameter> = emptyArray()
    protected var genericTs: List<KphpDocGenericParameterDecl> = emptyList()
    protected var classGenericTs: List<KphpDocGenericParameterDecl> = emptyList()

    protected var klass: PhpClass? = null
    protected var classGenericType: ExPhpTypeTplInstantiation? = null

    protected var argumentTypes: List<ExPhpType> = emptyList()
    protected var explicitGenericsT: List<ExPhpType> = emptyList()

    private val reifier = GenericReifier(project)

    /**
     * By the results from [unpack] instantiate template types and
     * returns the final Complete type.
     *
     * @return Complete type
     */
    protected abstract fun instantiate(): PhpType?

    /**
     * Unpacking data from [IndexingGenericCall.pack].
     *
     * @return true if data successfully unpacked, false otherwise
     */
    protected abstract fun unpack(packedData: String): Boolean

    /**
     * Resolves the given [incompleteType] and returns the Complete type.
     * If it can't resolve the received incomplete type, returns null.
     *
     * @return Complete type or null
     */
    fun resolve(incompleteType: String): PhpType? {
        val packedData = incompleteType.substring(2)
        if (!unpack(packedData)) {
            return null
        }

        reifier.reifyAllGenericsT(klass, parameters, genericTs, argumentTypes, null)
        return instantiate()
    }

    protected fun specialization(): MutableMap<String, ExPhpType> {
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

    protected fun beginCompleted(packedData: String): Boolean {
        return packedData.startsWith(IndexingGenericCall.START_TYPE + "\\")
    }

    protected fun safeSplit(data: String, count: Int, separator: String): List<String>? {
        val parts = data.split(separator)
        if (parts.size != count) return null
        return parts
    }

    protected fun resolveSubTypes(packedData: String): String {
        var data = packedData
            .removePrefix(IndexingGenericCall.START_TYPE.toString())
            .removeSuffix(IndexingGenericCall.END_TYPE.toString())

        val pairsParts = mutableListOf<MutableList<MutablePair<Int, Int>>>()

        val pairStack = Stack<MutablePair<Int, Int>>()
        for (i in data.indices) {

            if (data[i] == IndexingGenericCall.START_TYPE) {
                if (pairsParts.isEmpty()) {
                    pairsParts.add(mutableListOf())
                }

                val pair = MutablePair(i - 2, -1)
                pairsParts.last().add(pair)

                pairStack.add(pair)
            }

            if (data[i] == IndexingGenericCall.END_TYPE) {
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
