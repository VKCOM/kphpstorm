package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpDocTypeFromSuperMemberTP
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toExPhpType

object PsiToExPhpType {
    fun getTypeOfExpr(e: PsiElement, project: Project): ExPhpType? =
            when (e) {
                is PhpTypedElement -> e.type.toExPhpType(project)?.let { dropGenerics(it) }
                else               -> null
            }

    fun dropGenerics(type: ExPhpType): ExPhpType? {
        // TODO: добавить все типы
        if (type is ExPhpTypePipe) {
            val items = type.items.mapNotNull { dropGenerics(it) }
            if (items.isEmpty()) return null

            return ExPhpTypePipe(items)
        }

        if (type is ExPhpTypeTplInstantiation) {
            val list = type.specializationList.mapNotNull { dropGenerics(it) }
            if (list.isEmpty()) return null
            return ExPhpTypeTplInstantiation(type.classFqn, list)
        }

        if (type is ExPhpTypeNullable) {
            return dropGenerics(type.inner)?.let { ExPhpTypeNullable(it) }
        }

        if (type is ExPhpTypeArray) {
            return dropGenerics(type.inner)?.let { ExPhpTypeArray(it) }
        }

        if (type is ExPhpTypeGenericsT) {
            return null
        }

        return type
    }

    fun getFieldDeclaredType(field: Field, project: Project): ExPhpType? {
        val fieldType = field.docType.takeIf { !it.isEmpty } ?: field.type
        return fieldType.global(project).toExPhpType()
    }

    fun getArgumentDeclaredType(arg: Parameter, project: Project): ExPhpType? {
        val argType = arg.docType.takeIf { !it.isEmpty } ?: arg.type.takeIf { !it.isEmpty } ?: return null
        return argType.global(project).run { if (arg.isVariadic) unpluralize() else this }.toExPhpType()
    }

    fun getReturnDeclaredType(function: Function, project: Project): ExPhpType? {
        // function return type can be either in type declaration (.declaredType) or in phpdoc (.doctype)
        // also we need to traverse the tree up in case of inheritance:
        // class A { /** @return int */ function f(); } class B extends A { function f() { return 's'; } }
        // when function==B::f, it doesn't have neither type declaration nor doc type, so look at A::f
        // (in getArgumentDeclaredType() we don't need it, cause we use arg.type which does this automatically,
        //  function has also .type, but in contains inferred also, so we can't use it, as we need only declared)
        var returnType: PhpType? = null
        var curFunction: Function? = function
        while (returnType == null && curFunction != null) {
            val curReturnType = curFunction.declaredType.takeIf { !it.isEmpty } ?: curFunction.docType
            if (!curReturnType.isEmpty)
                returnType = curReturnType
            else if (curFunction is Method)
                curFunction = PhpDocTypeFromSuperMemberTP.superMethods(curFunction).firstOrNull()
            else
                break
        }

        return returnType?.toExPhpType(project)
    }
}
