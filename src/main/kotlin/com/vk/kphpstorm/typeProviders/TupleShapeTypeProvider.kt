package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ArrayHashElement
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.impl.ArrayAccessExpressionImpl
import com.jetbrains.php.lang.psi.elements.impl.MultiassignmentExpressionImpl
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.helpers.convertArrayIndexPsiToStringIndexKey
import com.vk.kphpstorm.helpers.toExPhpType


/**
 * Purpose:
 * kphp's type system is much richer that php's one.
 * For instance, we have tuples. '@return tuple(int, A|null)' for functions as an example. Same for shapes.
 * In PHP runtime it's just an array with zvals, but kphp handles each key separately, [0] is int, [1] is ?A.
 * We want to hack PhpStorm type inferring to support tuples and shapes.
 * E.g., get()[1]->... should suggest props/methods of class A, navigation and find usages should work.
 * list(, $b) = get(); $b->... — the same.
 *
 * For more detailed explanation see [ExPhpType]
 */
class TupleShapeTypeProvider : PhpTypeProvider4 {
    /**
     * Each type provider needs a unique key.
     * It is used for incomplete types:
     * "#{key}{custom_format}"
     * Here we use "#Й.{index} {lhs_type}"
     * E.g., "#Й.1 {return_type_of_get}"
     */
    override fun getKey(): Char {
        return 'Й'
    }

    /**
     * This function is called by PhpStorm for every PsiElement it wants to know type of.
     * We handle 2 cases:
     * 1) lhs[ index ]    — get type of lhs and call [ExPhpType.getSubkeyByIndex] (index)
     * 2) list(...) = rhs — get type of rhs and call it for indexes 0, 1, etc (presented on the left side)
     * Remember! We need to operate only local info here: no indexes usages, no project access, etc.
     * If lhs/rhs types are incomplete, we also return an incomplete type #Й..., to be decoded in [complete]
     */
    override fun getType(p: PsiElement): PhpType? {

        // 1) lhs[index]
        if (p is ArrayAccessExpressionImpl) {
            // detect index; we need only constant ones (Class::const unsupported as not real case)
            val indexPsi = p.index?.value ?: return null
            val indexKey = convertArrayIndexPsiToStringIndexKey(indexPsi) ?: return null
            if (indexKey.indexOf(' ') != -1)
                return null

            // detect lhs
            // its type can be incomplete: "#F\get" or "#P#E#F\get.b" or something like this
            val lhs = p.value
            val lhsType = (lhs as? PhpTypedElement)?.type ?: return null

            // but if it is complete — also accessible by local info @param/@var or @return of func in the same file —
            // calculate our type immediately and return it
            if (lhsType.isComplete)
                return inferTypeOfTupleShapeByIndex(lhsType, indexKey)

            // otherwise, save all info to be calculated in complete()
            // lhsType can be multiple (also separated by |), so create our own format for each of them
            val resultType = PhpType()
            lhsType.types.forEach {
                // PhpStorm native type providers also try to detect (with no lack, of course)
                // they also have unique keys and incomplete type format
                // BUT PhpType internals have a limit: only 50 subtypes (separated by |)
                // so, in complex scenarios like get()[1][2]->... combinations increase geometrically
                // to partially avoid this, use heruistics:
                // filter out subtypes detected by PhpStorm native type providers that are 100% useless here
                if (!it.contains("#π") && !it.contains("#E"))
                    resultType.add("#Й.$indexKey $it")
            }
//            println("type($lhs) = ${resultType.toString().replace("|", " | ")}")

            return resultType
        }

        // list(...) = rhs, [...] = rhs
        if (p is VariableImpl && PsiTreeUtil.getParentOfType(p, MultiassignmentExpressionImpl::class.java) != null) {
            val maExpr = PsiTreeUtil.getParentOfType(p, MultiassignmentExpressionImpl::class.java)!!
            val indexKey = detectIndexNearVarOfMultiassign(p, maExpr) ?: return null

            val rhs = (maExpr.value as? PhpExpressionImpl)?.value ?: return null
            val rhsType = (rhs as? PhpTypedElement)?.type ?: return null

            if (rhsType.isComplete)
                return inferTypeOfTupleShapeByIndex(rhsType, indexKey)

            val resultType = PhpType()
            rhsType.types.forEach {
                resultType.add("#Й.$indexKey $it")
            }
//            println("type($p) = ${resultType.toString().replace("|", " | ")}")

            return resultType
        }

        return null
    }

    /**
     * If we returned an incomplete type "#Й..." from [getType], complete() will be called.
     * Here we decode the string and we can access indexes and project.
     * So, we decode indexKey, know a complete lhs type and call [ExPhpType.getSubkeyByIndex]
     */
    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        val spacePos = incompleteTypeStr.indexOf(' ')
        val indexKey = incompleteTypeStr.substring(3, spacePos)
        val lhsTypeStr = incompleteTypeStr.substring(spacePos + 1)
        val wholeType = PhpType().add(lhsTypeStr).global(project)

        return inferTypeOfTupleShapeByIndex(wholeType, indexKey)
    }

    /**
     * This function is invoked to provide extra names linkage and references.
     * We do not need it for our purposes.
     */
    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?): MutableCollection<PhpNamedElement>? {
        return null
    }

    /**
     * Indicates that 'null' from complete() is a correct result.
     * Here it is so: getting index of 'int' for example leads to null.
     */
    override fun emptyResultIsComplete(): Boolean {
        return true
    }

    /**
     * Having list(..., $v, ...) = rhs — detect key near $v.
     * Don't forget about "key => $v", about short array syntax, about empties ",,,$v".
     */
    private fun detectIndexNearVarOfMultiassign(v: VariableImpl, maExpr: MultiassignmentExpressionImpl): String? {
        val mayBeArray = maExpr.firstPsiChild
        var numericIdx = 0
        var found = false
        var wasHash = false
        var indexPsi: PsiElement? = null

        // [...] = rhs
        if (mayBeArray is ArrayCreationExpression) {
            var child = mayBeArray.firstChild
            while (!found && child != null && child.elementType != PhpTokenTypes.opASGN) {
                if (child.elementType == PhpTokenTypes.opCOMMA) {
                    if (!wasHash)
                        numericIdx++
                    wasHash = false
                }
                else if (child is ArrayHashElement) {     // key => $v
                    wasHash = true
                    if (child.value == v) {
                        found = true
                        indexPsi = child.key
                    }
                }
                else if (child.elementType == PhpElementTypes.ARRAY_VALUE) {     // $v
                    if (child.firstChild == v) {
                        found = true
                    }
                }
                child = child.nextSibling
            }
        }
        // list(...) = rhs, very inconvenient to parse: it has just plain structure inside
        else {
            var child = maExpr.firstChild
            while (!found && child != null && child.elementType != PhpTokenTypes.opASGN) {
                if (child.elementType == PhpTokenTypes.opCOMMA) {
                    if (!wasHash)
                        numericIdx++
                    wasHash = false
                }
                else if (child.elementType == PhpTokenTypes.opHASH_ARRAY) {
                    wasHash = true
                }
                else if (child == v) {         // $v or key => $v, reserve search for key (suppose not $v=>)
                    found = true
                    val prev = PhpPsiUtil.getPrevSiblingIgnoreWhitespace(child, true)
                    if (prev != null && prev.elementType == PhpTokenTypes.opHASH_ARRAY)
                        indexPsi = PhpPsiUtil.getPrevSiblingIgnoreWhitespace(prev, true)
                }
                child = child.nextSibling
            }
        }

        if (!found)
            return null
        return if (indexPsi == null) numericIdx.toString() else convertArrayIndexPsiToStringIndexKey(indexPsi)
    }

    /**
     * The purpose of this type provider:
     * having PhpType — returned from custom psi format — and index — calc type by index in case of tuple/shape
     */
    private fun inferTypeOfTupleShapeByIndex(wholeType: PhpType, indexKey: String): PhpType? {
        // optimization: parse wholeType from string only if tuple/shape exist in it
        val needsCustomIndexing = wholeType.types.any {
            it.length > 7 && it[5] == '('           // tuple(, shape(, force(
                    || it == "\\kmixed"             // kmixed[*] is kmixed, not PhpStorm 'mixed' meaning uninferred
                    || it == "\\any"                // any[*] is any, not undefined
                    || it == "\\array"              // array[*] is any (untyped arrays)
        }
        if (!needsCustomIndexing)
            return null

        return wholeType.toExPhpType()?.getSubkeyByIndex(indexKey)?.toPhpType()
    }
}
