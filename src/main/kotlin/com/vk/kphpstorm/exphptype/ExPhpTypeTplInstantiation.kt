package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpClassHierarchyUtils
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * C<T...>, e.g. Wrapper<int>, Container<tuple<int, A>[]>, future<int>
 */
class ExPhpTypeTplInstantiation(val classFqn: String, val specializationList: List<ExPhpType>) : ExPhpType {
    override fun toString() = "$classFqn(${specializationList.joinToString(",")})"

    override fun toHumanReadable(expr: PhpPsiElement) = "${PhpCodeInsightUtil.createQualifiedName(PhpCodeInsightUtil.findScopeForUseOperator(expr)!!, classFqn)}(${specializationList.joinToString(",") { it.toHumanReadable(expr) }})"

    override fun toPhpType(): PhpType {
        return PhpType().add("$classFqn(${specializationList.joinToString(",")})")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return null
    }

    override fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType {
        val replacedSpecialization = specializationList.map { it.instantiateGeneric(nameMap) }
        return ExPhpTypeTplInstantiation(classFqn, replacedSpecialization)
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny              -> true
        // not finished
        is ExPhpTypePipe             -> rhs.items.any { it.isAssignableFrom(this, project) }
        is ExPhpTypeTplInstantiation -> classFqn == rhs.classFqn && specializationList.size == rhs.specializationList.size

        is ExPhpTypeInstance -> rhs.fqn == classFqn || run {
            val phpIndex = PhpIndex.getInstance(project)
            val lhsClass = phpIndex.getAnyByFQN(classFqn).firstOrNull() ?: return false
            var rhsIsChild = false
            phpIndex.getAnyByFQN(rhs.fqn).forEach { rhsClass ->
                PhpClassHierarchyUtils.processSuperWithoutMixins(rhsClass, true, true) { clazz ->
                    if (PhpClassHierarchyUtils.classesEqual(lhsClass, clazz))
                        rhsIsChild = true
                    !rhsIsChild
                }
            }
            rhsIsChild
        }
        else                         -> false
    }
}
