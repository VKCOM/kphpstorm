package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.generics.GenericUtil.genericInheritInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.genericParents
import com.vk.kphpstorm.generics.GenericUtil.getInstantiations
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.typeProviders.GenericMethodsTypeProvider
import java.lang.Integer.min

class ResolvingGenericMethodCall(project: Project) : ResolvingGenericCallBase(project) {
    private var method: Method? = null

    override fun instantiate(): PhpType? {
        val klass = klass ?: return null

        val specializationNameMap = specialization()

        val (extendsList, implementsList) = klass.genericParents()

        val parentsList = extendsList + implementsList
        parentsList.forEach { parent ->
            val extendsName = parent.fqn
            val genericNames = parent.genericNames()
            val inheritInstantiation = klass.genericInheritInstantiation(extendsName)
            if (inheritInstantiation != null) {
                val specList = inheritInstantiation.specializationList()

                val classSpecializationMap = genericNames.associate {
                    it.name to it.defaultType
                }.toMutableMap()

                for (i in 0 until min(genericNames.size, specList.size)) {
                    val genericT = genericNames[i]
                    val spec = specList[i]

                    classSpecializationMap[genericT.name] = spec
                }

                classSpecializationMap.forEach classForEach@{ (name, type) ->
                    if (type == null) {
                        return@classForEach
                    }
                    specializationNameMap[name] = type.instantiateGeneric(specializationNameMap)
                }
            }
        }

        val classImpl = klass as PhpClassImpl

        var returnTag: PhpDocReturnTag? = null

        val ifaces = classImpl.directImplementedInterfaces
        ifaces.forEach { iface ->
            val method = iface.findMethodByName(method!!.name)
            returnTag = method?.docComment?.returnTag ?: return@forEach
        }

        val classes = listOf(classImpl.superClass)
        classes.forEach { parent ->
            val method = parent?.findMethodByName(method!!.name)
            returnTag = method?.docComment?.returnTag ?: return@forEach
        }

        val classMethodReturnTag = method?.docComment?.returnTag
        if (classMethodReturnTag != null) {
            returnTag = classMethodReturnTag
        }

        val exType = returnTag?.type?.toExPhpType(project) ?: return null
        val specializedType = exType.instantiateGeneric(specializationNameMap)

        return specializedType.toPhpType()
    }

    override fun unpack(packedData: String): Boolean {
        // If PhpStorm resolved className and methodName:
        //   \SomeName.method...
        if (beginCompleted(packedData)) {
            val firstSeparator = packedData.indexOf(GenericMethodsTypeProvider.SEP)
            val fqn = packedData.substring(1, firstSeparator)
            val (className, methodName) = fqn.split('.')
            if (!className.contains("(") && !className.contains("|")) {
                klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull()
                method = klass?.findMethodByName(methodName)
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
        val instantiations = parsed?.getInstantiations()

        val foundInstantiation = instantiations?.firstOrNull {
            val klass = PhpIndex.getInstance(project).getClassesByFQN(it.classFqn).firstOrNull()
            val method = klass?.findMethodByName(methodName)

            method != null
        }

        val className = if (foundInstantiation != null && foundInstantiation.specializationList.first() !is ExPhpTypeGenericsT) {
            classGenericType = foundInstantiation
            foundInstantiation.classFqn
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
        argumentTypes = unpackTypeArray(parts[2])

        return true
    }
}
