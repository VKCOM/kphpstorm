package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.ClassConstantReference
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * Нативный вывод типов PhpStorm считает что `Foo::class` это строка и
 * это верно, однако ввиду такого упрощения мы теряем информацию и из-за
 * этого могут быть проблемы с выводом шаблонных параметров.
 *
 * Поэтому данный провайдер добавляет новый тип `class-string(T)` для
 * каждого выражения `Foo::class`.
 */
class ClassConstTypeProvider : PhpTypeProvider4 {
    override fun getKey() = '§'

    override fun getType(p: PsiElement): PhpType? {
        if (p is ClassConstantReference) {
            val classExType = p.classReference?.type?.toExPhpType()
            if (classExType != null) {
                return PhpType().add("force(class-string($classExType))")
            }
        }
        return null
    }

    override fun complete(incompleteType: String, project: Project) = null

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
