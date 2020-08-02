package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toStringAsNested

/**
 * "forcing" types is when my type provider knows better than native and wants to SHRINK native results.
 * For example, native bcdiv() is "string|null", but in kphp it is string, result is "string|null|force(string)".
 * @see com.vk.kphpstorm.typeProviders.FunctionsTypeProvider
 * When creating ExPhpType from such complex type, only forcing type will be left instead of pipe.
 *
 * This is used only for type compatibility [ExPhpTypePipe.isAssignableTo] and created only internally.
 * This can't be specified in phpdoc, that's why it doesn't have matching psi realization class.
 */
class ExPhpTypeForcing(val inner: ExPhpType) : ExPhpType {
    override fun toString() = "force($inner)"

    override fun toHumanReadable(expr: PhpPsiElement) = inner.toHumanReadable(expr)

    override fun toPhpType(): PhpType {
        return PhpType().add("force(${inner.toPhpType().toStringAsNested()})")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return ExPhpTypeForcing(inner.getSubkeyByIndex(indexKey) ?: return null)
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeForcing(inner.instantiateTemplate(nameMap))
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean {
        // if we explicitly write force(T) in phpdoc, it means that PhpStorm fails detecting T,
        // that's why everything is assignable to forcing type, making stricy typing inspections assume all is ok
        return true
    }
}
