package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.typeProviders.GenericMethodsTypeProvider

class ResolvingGenericMethodCall(project: Project) : ResolvingGenericBase(project) {
    override var klass: PhpClass? = null
    private var method: Method? = null

    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>
    override lateinit var classGenericTs: List<KphpDocGenericParameterDecl>
    override var classGenericType: ExPhpTypeTplInstantiation? = null

    override fun instantiate(): PhpType? {
        val specializationNameMap = specialization()

        val returnTag = method?.docComment?.returnTag ?: return null
        val exType = returnTag.type.toExPhpType() ?: return null
        val specializedType = exType.instantiateGeneric(specializationNameMap)

        return specializedType.toPhpType()
    }
    
    /**
     * См. комментарий в [ResolvingGenericFunctionCall.unpackImpl]
     */
    override fun unpackImpl(packedData: String): Boolean {
        // If className and methodName are resolved
        // $START_TYPE\SomeName.method...
        if (beginCompleted(packedData)) {
            val firstSeparator = packedData.indexOf(GenericMethodsTypeProvider.SEP)
            val fqn = packedData.substring(1, firstSeparator)
            val (className, methodName) = fqn.split('.')
            if (!className.contains("(") && !className.contains("|")) {
                klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull()
                method = klass?.findMethodByName(methodName) ?: return false
                if (method?.isReturnGeneric() == false)
                    return false
            }
        }

        val data = resolveSubTypes(packedData)
        val parts = safeSplit(data, 3, GenericMethodsTypeProvider.SEP) ?: return false

        val fqn = parts[0]

        val dotIndex = fqn.lastIndexOf('.')
        val classRawName = fqn.substring(0, dotIndex)
        val methodName = fqn.substring(dotIndex + 1)

        val classType = PhpType().add(classRawName).global(project)
        val parsed = classType.toExPhpType()
        val instantiation = parsed?.getInstantiation()

        val className = if (instantiation != null && instantiation.specializationList.first() !is ExPhpTypeGenericsT) {
            classGenericType = instantiation
            instantiation.classFqn
        } else {
            classRawName
        }

        if (klass == null) {
            klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull() ?: return false
        }
        if (method == null) {
            method = klass!!.findMethodByName(methodName)
        }
        if (method == null) {
            return false
        }

        parameters = method!!.parameters
        genericTs = method!!.genericNames()

        // Не устанавливаем параметры класса, так как это статический вызов
        if (!method!!.isStatic) {
            classGenericTs = klass!!.genericNames()
        }

        explicitGenericsT = unpackTypeArray(parts[1])
        argumentsTypes = unpackTypeArray(parts[2])

        return true
    }
}
