package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.configuration.KphpStormConfiguration

/**
 * ANY is a special type which can be assigned to everything, and everything can be assigned to it.
 * For example, "array" in phpdoc is parsed as any[].
 * Note! "mixed" in phpdoc is "var", that's why "mixed" can be emerged only by PhpStorm inferring 
 */
class ExPhpTypeAny : ExPhpType {
    override fun toString() = "any"

    override fun toHumanReadable(expr: PhpPsiElement) =
            // for plain PHP projects, "any" is mostly emerged by "mixed", which is not "var", but "mixed"
            if (KphpStormConfiguration.wasSetupForProject(expr.project)) "any"
            else "mixed"

    override fun equals(other: Any?) = other is ExPhpTypeAny

    override fun hashCode() = 31


    override fun toPhpType(): PhpType {
        return KphpPrimitiveTypes.PHP_TYPE_ANY
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return this
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return this
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean {
        // anything can be assigned to any
        return true
    }
}
