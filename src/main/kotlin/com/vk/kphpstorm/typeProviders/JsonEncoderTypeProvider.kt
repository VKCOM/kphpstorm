package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.ClassConstantReference
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4

/**
 * Purpose:
 * 1) infer the correct type based on the second argument of the JsonEncoder::decode()
 */
class JsonEncoderTypeProvider : PhpTypeProvider4 {
    override fun getKey() = '♂'

    override fun getType(p: PsiElement?): PhpType? {
        if (p !is MethodReference) {
            return null
        }

        if (p.classReference == null) {
            return null
        }

        if (p.name != "decode") {
            return null
        }

        val classRef = p.classReference
        if (classRef !is ClassReference) {
            return null
        }

        val classNameParameter = p.parameterList?.getParameter(1) ?: return null
        if (classNameParameter !is ClassConstantReference) {
            return null
        }

        val type = classNameParameter.classReference?.type

        if (classRef.name != "JsonEncoder") {
            return PhpType().add("#" + key + classRef.fqn + key + type)
        }

        return type
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        val indexOfSign = incompleteTypeStr.indexOf(key)
        val indexOfDelimiter = incompleteTypeStr.indexOf(key, indexOfSign + 1)
        val classFqn = incompleteTypeStr.substring(indexOfSign + 1, indexOfDelimiter)
        val type = incompleteTypeStr.substring(indexOfDelimiter + 1)

        val phpClass = PhpIndex.getInstance(project).getClassesByFQN(classFqn).firstOrNull()
        if (phpClass !is PhpClass) {
            return null
        }

        val referenceElement = phpClass.extendsList.referenceElements

        val extendElement = referenceElement.getOrNull(0) ?: return null
        if (extendElement.name != "JsonEncoder") {
            return PhpType().add("#" + key + extendElement.fqn + key + type)
        }

        return PhpType().add(type)
    }

    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?) = null
}
