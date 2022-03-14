package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * T1|T2|... — T... can be any types (e.g. int[]|false|null)
 */
class ExPhpTypePipe(val items: List<ExPhpType>) : ExPhpType {
    override fun toString() = items.joinToString("|")

    override fun toHumanReadable(expr: PhpPsiElement): String {
        val presentationItems = items.toMutableList()

        // PhpStorm combines @return and actual types from 'return' statements:
        // /** @return float */ function f() { return 5; return '5'; }
        // it will infer f() : float|int|string — as it calls PhpType.add() for every available
        // but consider functions returning tuple:
        // /** @return tuple(float, A) */ function f() { return tuple(4, new A); }
        // it will infer f() : tuple(float,A)|tuple(int,A), because PhpType.add() just compares strings
        // so, return type of f() is such pipe, but here — for presentation — concat these tuples into one
        val hasManyTuples = presentationItems.count { it is ExPhpTypeTuple } > 1
        if (hasManyTuples) {
            val project = expr.project
            val maxTuple = presentationItems.maxByOrNull { if (it is ExPhpTypeTuple) it.items.size else 0 } as ExPhpTypeTuple
            val mergedPhpTypes = maxTuple.items.mapTo(arrayListOf()) { PhpType() }
            for (index in mergedPhpTypes.indices)
                for (item in presentationItems)
                    if (item is ExPhpTypeTuple && item.items.size > index)
                        mergedPhpTypes[index].add(item.items[index].toPhpType())
            presentationItems.removeAll { it is ExPhpTypeTuple }
            presentationItems.add(ExPhpTypeTuple(mergedPhpTypes.map { it.toExPhpType(project) ?: ExPhpType.ANY }))
        }

        // for shapes this is easier, because shape([...]) invocation PhpType just returns "shape()"
        // /** @return shape(x:int) */ function f() { return shape([...]); }
        // PhpStorm will infer shape(x:int)|shape(), so just remove empty shapes
        val hasManyShapes = presentationItems.count { it is ExPhpTypeShape } > 1
        if (hasManyShapes) {
            val maxShape = presentationItems.maxByOrNull { if (it is ExPhpTypeShape) it.items.size else 0 } as ExPhpTypeShape
            if (maxShape.items.isNotEmpty()) {      // for strange situations like int|shape() don't remove it
                presentationItems.removeAll { it is ExPhpTypeShape && it.items.isEmpty() }
            }
        }

        // make |false appear on the right, int| on the left and so on
        presentationItems.sortBy { a ->
            when {
                a === ExPhpType.ANY    -> 15
                a === ExPhpType.NULL   -> 14
                a === ExPhpType.FALSE  -> 13
                a === ExPhpType.KMIXED -> 12

                a === ExPhpType.INT    -> -10
                a === ExPhpType.FLOAT  -> -9
                a === ExPhpType.STRING -> -8
                a === ExPhpType.BOOL   -> -7

                else                   -> 0
            }
        }

        return presentationItems.joinToString("|") { it.toHumanReadable(expr) }
    }

    override fun toPhpType(): PhpType {
        val result = PhpType()
        for (i in items)
            result.add(i.toPhpType())
        return result
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        val nnItems = items.mapNotNull { it.getSubkeyByIndex(indexKey) }
        return when (nnItems.size) {
            0    -> null
            1    -> nnItems[0]
            else -> ExPhpTypePipe(nnItems)
        }
    }

    override fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypePipe(items.map { it.instantiateGeneric(nameMap) })
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean {
        // let this == "mixed|string[]"; what rhs can be assigned to this?
        // "string" — ok (because of mixed), "int" ok, "string[]" ok, "?int" ok, "A" not, "string[]|A" not, "?tuple" not
        return when (rhs) {
            is ExPhpTypeAny      -> true
            is ExPhpTypePipe     -> rhs.items.all { // for false/bool see below
                it === ExPhpType.FALSE || it === ExPhpType.BOOL || items.any { lhsIt -> lhsIt.isAssignableFrom(it, project) }
            }
            is ExPhpTypeNullable -> items.any { it.isAssignableFrom(ExPhpType.NULL, project) } && items.any { it.isAssignableFrom(rhs.inner, project) }
            else                 -> items.any { it.isAssignableFrom(rhs, project) }
        }
    }

    /**
     * Heruistics: let this == "string|int", where can this be assigned to?
     * To "string" — no, "int" not compatible. To "string|float" — yes. To "string|int|A" — yes.
     * Let this == "string|null". To "string" — no, to "?string" — yes.
     * So, all items in pipe must be compatible with lhs.
     * Naive (but incorrect) realization:
     * return items.all { lhs.isAssignableFrom(it) }
     *
     * But! There are exceptions, see comments.
     * Also remember about [ExPhpTypeForcing] which can be created instead of pipe.
     */
    fun isAssignableTo(lhs: ExPhpType, project: Project): Boolean {
        var ok = items.all { lhs.isAssignableFrom(it, project) }

        // exception 1: incorrect handling '|false' and no smartcasts for false
        // is invalid after 2020.2
        // before, PhpStorm treated 'false' as 'bool', and I allowed to mix anything with bool

        // exception 2; (int|false)[] is expressed as int[]|false[] in terms of PhpType, which is not equivalent
        // before 2020.2, I tried to convert T1[]|T2[]|... to (T1|T2|...)[] and try once again 
        // now not needed

        // exception 3: traits and child classes
        // in construction { $o = new Base; if($o instanceof Derived) { ... } } - inside if PhpStorm infers $o as Base|Derived
        // in general, we don't want to allow Base to be assignable to Derived, but let Base|Derived be
        // (though it contraverses with logic "return items.all { lhs.isAssignableFrom(it) }")
        // so, heruistics: choose assignable instances and check that others are assignable to any of them
        if (!ok) {
            val allInstances = items.all { it === ExPhpType.NULL || it === ExPhpType.ANY || it === ExPhpType.OBJECT || it is ExPhpTypeInstance }
            if (allInstances) {
                val assignable = items.filter { lhs.isAssignableFrom(it, project) }
                if (assignable.isNotEmpty() && assignable.all { okClass ->
                            items.any { !assignable.contains(it) && it.isAssignableFrom(okClass, project) }
                        })
                    ok = true
            }

            if (ExPhpTypeClassString.isNativePipeWithString(this)) {
                return lhs.isAssignableFrom(ExPhpTypeClassString.getClassFromNativePipeWithString(this), project)
            }
        }

        return ok
    }
}
