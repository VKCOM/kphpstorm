package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
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

        if (classRef.name != "JsonEncoder") {
            val phpClass = classRef.resolve()
            if (phpClass !is PhpClass) {
                return null
            }

            val referenceElement = phpClass.extendsList.referenceElements

            val extendElement = referenceElement.getOrNull(0) ?: return null
            if (extendElement.name != "JsonEncoder") {
                return null
            }
        }

        val classNameParameter = p.parameterList?.getParameter(1) ?: return null
        if (classNameParameter !is ClassConstantReference) {
            return null
        }

        return classNameParameter.classReference?.type
    }

    override fun complete(incompleteTypeStr: String, project: Project) = null

    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?) = null

}
