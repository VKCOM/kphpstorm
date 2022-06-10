package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpCharTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTplInstantiationPsiImpl
import com.vk.kphpstorm.generics.GenericUtil.genericInheritInstantiation
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericConstructorCall

class GenericClassesTypeProvider : PhpTypeProvider4 {
    companion object {
        const val SEP = "―"
        val KEY = PhpCharTypeKey('±')
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        // new A/*<...args>*/()
        if (p is NewExpression) {
            val classRef = p.classReference ?: return null
            val fqn = classRef.fqn + ".__construct"
            val data = IndexingGenericFunctionCall(fqn, p.parameters, p, SEP).pack()
            return PhpType().add(KEY.sign(data))
        }

        if (p is ClassReference && p.name == "parent") {
            val containingClass = p.parentOfType<PhpClass>()
            if (containingClass != null) {
                val superClass = containingClass.extendsList.referenceElements.firstOrNull() ?: return null
                val superClassName = superClass.fqn ?: return null
                val instantiationParameter = containingClass.genericInheritInstantiation(superClassName)
                val instantiation = instantiationParameter?.firstChild as? ExPhpTypeTplInstantiationPsiImpl
                return instantiation?.type
            }
        }

        return null
    }

    override fun complete(incompleteType: String, project: Project) =
        ResolvingGenericConstructorCall(project).resolve(incompleteType)

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
