package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericFunctionUtil.genericNames
import com.vk.kphpstorm.generics.GenericFunctionUtil.isReturnGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import kotlin.math.min

class GenericFunctionsTypeProvider : PhpTypeProvider4 {
    companion object {
        private val KEY = object : PhpCharBasedTypeKey() {
            override fun getKey(): Char {
                return 'П'
            }
        }
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement): PhpType? {
        if (p !is FunctionReference) {
            return null
        }

        val call = GenericFunctionCall(p)
        call.resolveFunction()
        if (call.function == null) return null

        // Если возвращаемый тип функции не зависит от шаблонного типа,
        // то нет смысла как-то уточнять ее тип.
        if (!call.function!!.isReturnGeneric()) {
            return null
        }

        val specs = call.explicitSpecs.ifEmpty { call.implicitSpecs }

        val specTypes = specs.joinToString(",")

        return PhpType().add("#П${call.function!!.fqn}<$specTypes>")
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        if (!KEY.signed(incompleteTypeStr)) {
            return null
        }

        val functionName = incompleteTypeStr.substring(2 until incompleteTypeStr.indexOf('<'))
        val function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull() ?: return null

        val lhsTypeStr = incompleteTypeStr.substring(2)
        val lhsType = PhpType().add(lhsTypeStr).global(project)
        val parsed = lhsType.toExPhpType()

        val instantiation = when (parsed) {
            is ExPhpTypePipe -> parsed.items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> parsed.inner
            else -> parsed
        } as? ExPhpTypeTplInstantiation ?: return null

        val docTNames = function.genericNames()

        val specializationNameMap = mutableMapOf<String, ExPhpType>()

        for (i in 0 until min(docTNames.size, instantiation.specializationList.size)) {
            specializationNameMap[docTNames[i]] = instantiation.specializationList[i]
        }

        val methodReturnTag = function.docComment?.returnTag ?: return null
        val methodTypeParsed = methodReturnTag.type.toExPhpType() ?: return null
        val methodTypeSpecialized = methodTypeParsed.instantiateGeneric(specializationNameMap)

        return methodTypeSpecialized.toPhpType()
    }

    override fun getBySignature(
        typeStr: String,
        visited: MutableSet<String>?,
        depth: Int,
        project: Project?
    ): MutableCollection<PhpNamedElement>? {
        return null
    }
}
