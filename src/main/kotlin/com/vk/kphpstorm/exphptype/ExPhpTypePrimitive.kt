package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Leaf type, e.g. 'int', 'string', 'var' – primitives only! Not instances!
 */
class ExPhpTypePrimitive(val typeStr: String) : ExPhpType {
    override fun toString() = typeStr

    override fun toHumanReadable(expr: PhpPsiElement) = typeStr

    override fun toPhpType(): PhpType {
        return KphpPrimitiveTypes.mapPrimitiveToPhpType[typeStr]
                ?: PhpType().add(typeStr)       // not supposed to happen
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return if (this === ExPhpType.VAR || this === ExPhpType.STRING) this else null
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return this
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny       -> true
        is ExPhpTypePipe      -> rhs.isAssignableTo(this, project)
        is ExPhpTypePrimitive -> canBeAssigned(this, rhs)
        is ExPhpTypeNullable  -> canBeAssigned(this, ExPhpType.NULL) && isAssignableFrom(rhs.inner, project)
        is ExPhpTypeArray     -> canBeAssigned(this, ExPhpType.VAR) && isAssignableFrom(rhs.inner, project)
                || this === ExPhpType.CALLABLE  // ['class', 'name'] is assignable to "callable" :(
        is ExPhpTypeInstance  -> this === ExPhpType.OBJECT
        is ExPhpTypeForcing   -> isAssignableFrom(rhs.inner, project)
        else                  -> false
    }

    companion object {
        private fun canBeAssigned(l: ExPhpTypePrimitive, r: ExPhpTypePrimitive) = with(ExPhpType.Companion) {
            when {
                l === INT      -> r === INT || r === FLOAT
                l === FLOAT    -> r === INT || r === FLOAT
                l === STRING   -> r === STRING
                l === BOOL     -> r === FALSE || r === BOOL
                l === FALSE    -> r === FALSE
                l === NULL     -> r === NULL
                l === OBJECT   -> r === OBJECT || r === NULL
                l === CALLABLE -> r === CALLABLE || r === STRING
                l === VOID     -> r === VOID
                l === VAR      -> r === INT || r === FLOAT || r === STRING || r === BOOL || r === FALSE || r === NULL || r === VAR

                else           -> false       // not supposed to happen
            }
        }
    }
}
