package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpClassHierarchyUtils
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * 'A', '\asdf\name' — are instances (not primitives!)
 */
class ExPhpTypeInstance(val fqn: String) : ExPhpType {
    override fun toString() = fqn

    override fun toHumanReadable(expr: PhpPsiElement) =
        if (fqn.startsWith('\\')) PhpCodeInsightUtil.createQualifiedName(
            PhpCodeInsightUtil.findScopeForUseOperator(expr)!!,
            fqn
        )
        else fqn

    override fun toPhpType(): PhpType {
        return PhpType().add(fqn)
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return null
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return nameMap[fqn] ?: this
    }
    private fun canBeAssigned(l: ExPhpTypeInstance, r: ExPhpTypeInstance) = with(ExPhpType.Companion) {
        when {
            l === ExPhpTypeInstance(KphpPrimitiveTypes.OBJECT)   -> r === ExPhpTypeInstance(KphpPrimitiveTypes.OBJECT)
            else           -> false       // not supposed to happen
        }
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny       -> true
        is ExPhpTypePipe      -> rhs.isAssignableTo(this, project)
        is ExPhpTypeNullable -> {
         /*   if(this === ExPhpType.OBJECT){

            }
            rhs === ExPhpType.OBJECT || rhs === ExPhpType.NULL*/
            /*ExPhpType.OBJECT
            val OBJECT = ExPhpTypeInstance(KphpPrimitiveTypes.OBJECT)
            rhs === OBJECT || rhs === ExPhpType.NULL*/
            /*if(this as ExPhpTypeNullable && rhs is ExPhpTypeNullable){
                false
            }*/
            false
            /*val t = canBeAssigned(this, ExPhpTypeInstance(KphpPrimitiveTypes.NULL))
           if( this === ExPhpTypeInstance(KphpPrimitiveTypes.OBJECT)){
               rhs === ExPhpTypeInstance(KphpPrimitiveTypes.NULL)
           }else{
               isAssignableFrom(rhs.inner, project)
           }*/
           /* canBeAssigned(this, ExPhpTypeInstance(KphpPrimitiveTypes.NULL))
          val t =  this.fqn
            if (rhs.toString() != fqn) {
                false
            } else {
                isAssignableFrom(rhs.inner, project)
            }*/
        }

        is ExPhpTypePrimitive -> rhs === ExPhpType.NULL || rhs === ExPhpType.OBJECT

        // rhs can be assigned if: rhs == lhs or rhs is child of lhs (no matter, lhs is interface or class)
        is ExPhpTypeInstance  -> rhs.fqn == fqn || run {
            val phpIndex = PhpIndex.getInstance(project)
            val lhsClass = phpIndex.getAnyByFQN(fqn).firstOrNull() ?: return false
            var rhsIsChild = false
            phpIndex.getAnyByFQN(rhs.fqn).forEach { rhsClass ->
                PhpClassHierarchyUtils.processSuperWithoutMixins(rhsClass, true, true) { clazz ->
                    if (PhpClassHierarchyUtils.classesEqual(lhsClass, clazz))
                        rhsIsChild = true
                    !rhsIsChild
                }
            }
            rhsIsChild
        }

        else                  -> false
    }
}
