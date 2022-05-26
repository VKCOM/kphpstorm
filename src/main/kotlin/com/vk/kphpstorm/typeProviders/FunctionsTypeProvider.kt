package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.helpers.toStringAsNested

/**
 * Purpose:
 * 1) handle not_null(), tuple(), instance_cache_fetch() and other kphp-builtin function calls —
 *    infer types when we can't express in phpdocs when writing php polyfills
 * 2) there are lots of bugs in php stdlib inferring and phpstorm.meta.php
 *    (for example: https://youtrack.jetbrains.com/issue/WI-53703 WI-53748 and many others).
 *    Here we fix noticable bugs by "forcing types" (see below)
 * 3) sometimes kphp's function return types are slightly different for some functions.
 *    For example, bcdiv() returns string in kphp, but by php spec (and in PhpStorm) it returns ?string.
 *    That's why $o->str_field = bcdiv(...) compiles in kphp, but without hacking shows type compat error.
 *    Here we fix this also by "forcing types".
 *
 * What is "forcing"?
 * Type provider can only EXTEND typing, but we need to SHRINK (in general, to REPLACE):
 * PhpStorm already thinks bcdiv() is "string|null", we want it to think that bcdiv() is just "string".
 * I invented such hack: EXTEND type, so that PhpStorm thinks that bcdiv() is "string|null|force(string)",
 * and my type compatibility checkers know about forcing, and if exists, drop other cases
 * @see PhpTypeToExPhpTypeParsing.createPipeOrSimplified
 * PhpType is ugly, but hovering and "type info" actions are custom, showing human-readable types, not raw PhpType.
 *
 * Forcing is for cases 2 and 3: when we need to replace return types of functions PhpStorm already knows about.
 *
 * The main disadvantage of such approach:
 * $x = 1 ? bcdiv(...) : [1]
 * Will be inferred as "string|null|force(string)|int[]" -> this is "string", not "string|int[]".
 *
 * To read about type providers in general, see [TupleShapeTypeProvider].
 */
class FunctionsTypeProvider : PhpTypeProvider4 {
    private val phpTypeForceString = PhpType.PhpTypeBuilder().add("force(string)").build()
    private val phpTypeForceStringArray = PhpType.PhpTypeBuilder().add("force(string[])").build()
    private val phpTypeForceKMixed = PhpType.PhpTypeBuilder().add("force(kmixed)").build()
    private val phpTypeForceInt = PhpType.PhpTypeBuilder().add("force(int)").build()
    private val phpTypeForceFloat = PhpType.PhpTypeBuilder().add("force(float)").build()

    private val CUSTOM_HANDLED_FUNCTIONS: List<Pair<String, String>> = listOf(
            // kphp keywords and language constructs
            "not_null" to "manual",
            "not_false" to "manual",
            "tuple" to "manual",
            "shape" to "manual",

            // kphp functions from functions.txt with non-trivial return types
            "array_first_value" to "^1[*]",
            "array_last_value" to "^1[*]",
            "array_filter_by_key" to "^1",
            "instance_cache_fetch" to "instance<^1>",
            "instance_cache_fetch_immutable" to "instance<^1>",
            "instance_cast" to "instance<^2>",
            "instance_deserialize" to "instance<^2>",
//            "array_find" to "manual",

            // php stdlib functions which return type is different from kphp's one
            "bcdiv" to "string",
            "bcmod" to "string",
            "get_class" to "string",
            "date" to "string",
            "gmdate" to "string",
            "hex2bin" to "string",
            "pack" to "string",
            "ob_get_contents" to "string",
            "substr_replace" to "string",
            "var_export" to "string",
            "explode" to "string[]",
            "unserialize" to "kmixed",        // not 'mixed'
            "json_decode" to "kmixed",
            "mysqli_query" to "kmixed",
            "getimagesize" to "kmixed",
            "array_rand" to "kmixed",
            "parse_url" to "kmixed",
            "preg_replace" to "kmixed",
            "preg_replace_callback" to "kmixed",
            "curl_init" to "int",
            "bindec" to "int",
            "mysqli_insert_id" to "int",
            "unpack" to "array",
            "floor" to "float",
            "round" to "float",

            // php stdlib functions as replacement of phpstorm.meta.php
            "str_replace" to "^3",
            "array_slice" to "^1",
            "array_chunk" to "^1[]",
            "array_splice" to "^1",
            "array_intersect_key" to "^1",
            "array_intersect" to "^1",
            "array_intersect_assoc" to "^1",
            "array_diff_key" to "^1",
            "array_diff" to "^1",
            "array_diff_assoc" to "^1",
            "array_reverse" to "^1",
            "array_shift" to "^1[*]",
            "array_values" to "^1",
            "array_unique" to "^1",
            "array_fill" to "^3[]",
            "array_fill_keys" to "^2[]",
            "array_combine" to "^2",
            "array_pop" to "^1[*]",
            "array_filter" to "^1",
            "array_pad" to "array",
            "array_column" to "array"
    )

    private val FUNC_NAMES_INDEX = CUSTOM_HANDLED_FUNCTIONS.map { it.first }.toSortedSet()

    override fun getKey(): Char {
        return '!'
    }

    override fun getType(p: PsiElement): PhpType? {
        val isCustomHandledFunc = p is FunctionReference && p !is MethodReference && FUNC_NAMES_INDEX.contains(p.name)
        if (!isCustomHandledFunc)
            return null
        p as FunctionReference

        val funcName = p.name
        val parameters = p.parameters
        val format = CUSTOM_HANDLED_FUNCTIONS.find { it.first == funcName }!!.second

        if (format == "string")
            return phpTypeForceString
        if (format == "string[]")
            return phpTypeForceStringArray
        if (format == "kmixed")
            return phpTypeForceKMixed
        if (format == "int")
            return phpTypeForceInt
        if (format == "float")
            return phpTypeForceFloat
        if (format == "array")
            return KphpPrimitiveTypes.PHP_TYPE_ARRAY_OF_ANY     // don't force here, just any[]

        // ^1[]
        if (format.startsWith('^') && format.endsWith("[]")) {
            val argIndex = format.substring(1, format.length - 2).toInt() - 1
            val argType = getArgType(parameters, argIndex) ?: return null

            return when {
                argType.isComplete -> inferTypeArrayOf(argType)
                else               -> PhpType().apply {
                    argType.types.forEach {
                        if (!it.contains("#F"))
                            add("#!a $it")
                    }
                }
            }
        }

        // ^1[*]
        if (format.startsWith('^') && format.endsWith("[*]")) {
            val argIndex = format.substring(1, format.length - 3).toInt() - 1
            val argType = getArgType(parameters, argIndex) ?: return null

            return when {
                argType.isComplete -> inferTypeElementOf(argType)
                else               -> PhpType().apply {
                    argType.types.forEach {
                        if (!it.contains("#F"))
                            add("#!e $it")
                    }
                }
            }
        }

        // ^1
        if (format.startsWith('^')) {
            val argIndex = format.substring(1, format.length).toInt() - 1
            val argType = getArgType(parameters, argIndex) ?: return null

            return when {
                argType.isComplete -> inferTypeSameAs(argType)
                else               -> PhpType().apply {
                    argType.types.forEach {
                        if (!it.contains("#F"))
                            add("#!s $it")
                    }
                }
            }
        }

        // instance<^1>
        if (format.startsWith("instance")) {
            val argIndex = format.substring(10, format.length - 1).toInt() - 1
            if (argIndex >= parameters.size) return null
            // arg expected to be A::class, its psi is "A" — ClassReference
            val arg = parameters[argIndex] as? ClassConstantReference ?: return null
            val clRef = arg.children.firstOrNull() as? ClassReference ?: return null

            return if (arg.name == "class") {
                // having 'A::class', A may be relative (due to top-level uses)
                // I suppose that getting .fqn is ok here in getType() (assume it doesn't use index lookups)
                return PhpType().add(clRef.fqn)
            }
            else null
        }

        // parse manual

        // not_null() converts T|null to T (instances are all still nullable)
        if (funcName == "not_null") {
            val argType = getArgType(parameters, 0) ?: return null

            return when {
                argType.isComplete -> inferNotNull(argType)
                else               -> PhpType().apply {
                    argType.types.forEach { add("#!n $it") }
                }
            }
        }

        // not_false() converts T|false to T
        if (funcName == "not_false") {
            val argType = getArgType(parameters, 0) ?: return null

            return when {
                argType.isComplete -> inferNotFalse(argType)
                else               -> PhpType().apply {
                    argType.types.forEach { add("#!f $it") }
                }
            }
        }

        // for tuple(..., ...) infer type of every argument
        if (funcName == "tuple") {
            val parameterTypes = parameters.indices.map {
                getArgType(parameters, it) ?: KphpPrimitiveTypes.PHP_TYPE_ANY
            }

            return when {
                parameterTypes.all { it.isComplete } -> inferTuple(parameterTypes)
                else                                 -> PhpType().add(
                        // use special chars do decode parameters and pipes in a single string; avoid | and even / characters
                        "#!t ${parameterTypes.joinToString("ꄳ") { it.toStringAsNested("⎋") }}")
            }
        }

        // ввиду шаблонов нам может понадобиться точный тип для шейпов, поэтому мы выводим его здесь
        if (funcName == "shape") {
            val innerArray = p.parameters.firstOrNull() as? ArrayCreationExpression ?: return null

            var containsUnresolved = false
            val types = ArrayCreationExpressionImpl.children(innerArray).mapNotNull {
                if (it !is ArrayHashElement) return@mapNotNull null
                if (it.value !is PhpTypedElement) return@mapNotNull null
                if (it.key == null) return@mapNotNull null
                if (it.key !is StringLiteralExpression) return@mapNotNull null

                val key = (it.key as StringLiteralExpression).contents
                val type = (it.value as PhpTypedElement).type
                if (!type.isComplete) {
                    containsUnresolved = true
                }

                Pair(key, type)
            }

            if (containsUnresolved) {
                return PhpType().add(
                    "#!h ${types.joinToString("ꄴ") { it.first + ":" + it.second.toStringAsNested("ꄶ") }}"
                )
            }

            return inferShape(types)
        }

//        println("unhandled function: $funcName")
        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        val funcChar = incompleteTypeStr[2]
        val argTypeStr = incompleteTypeStr.substring(4)

        // inferring for "tuple(...)" needs special decoding: any arguments are encoded to a single string
        if (funcChar == 't') {
            val parameterTypes = argTypeStr.split('ꄳ').map {
                PhpType().apply {
                    it.split('⎋').forEach { add(it) }
                }.global(project)
            }
            return inferTuple(parameterTypes)
        }

        // inferring for "shape(...)" needs special decoding: any arguments are encoded to a single string
        if (funcChar == 'h') {
            val parameterTypes = argTypeStr.split('ꄴ').map {
                val (key, unresolvedType) = it.split(':')
                val type = PhpType().apply {
                    unresolvedType.split('ꄶ').forEach { add(it) }
                }.global(project)

                Pair(key, type)
            }
            return inferShape(parameterTypes)
        }

        // for all other cases, just a single argument is encoded
        val argType = PhpType().add(argTypeStr).global(project)

        return when (funcChar) {
            's'  -> inferTypeSameAs(argType)
            'a'  -> inferTypeArrayOf(argType)
            'e'  -> inferTypeElementOf(argType)
            'n'  -> inferNotNull(argType)
            'f'  -> inferNotFalse(argType)
            't'  -> null        // handled above
            else -> null
        }
    }

    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?): MutableCollection<PhpNamedElement>? {
        return null
    }


    private fun PhpType.force(): PhpType {
        val type = this.toExPhpType() ?: return this

        if (type.isGeneric()) {
            return PhpType().add(this)
        }

        return when(type) {
            is ExPhpTypeForcing  -> this
            is ExPhpTypeInstance -> this
            else                 -> PhpType().add("force($this)")
        }
    }

    private fun getArgType(parameters: Array<PsiElement>, argIndex: Int): PhpType? {
        val arg = if (argIndex < parameters.size) parameters[argIndex] else return null
        val argType = (arg as? PhpTypedElement)?.type ?: return null
        return if (argType.isEmpty) KphpPrimitiveTypes.PHP_TYPE_ANY else argType
    }


    private fun inferTypeSameAs(argType: PhpType): PhpType {
        return argType.force()
    }

    private fun inferTypeArrayOf(argType: PhpType): PhpType {
        return argType.toExPhpType().let {
            if (it === null) PhpType.EMPTY
            else ExPhpTypeArray(it).toPhpType().force()
        }
    }

    private fun inferTypeElementOf(argType: PhpType): PhpType {
        return argType.toExPhpType()?.getSubkeyByIndex("0")?.toPhpType()?.force() ?: KphpPrimitiveTypes.PHP_TYPE_ANY
    }

    private fun inferNotNull(argType: PhpType): PhpType {
        return PhpType().apply {
            for (t in argType.types)
                if (t != "\\null")
                    add(t)
        }
    }

    private fun inferNotFalse(argType: PhpType): PhpType {
        return PhpType().apply {
            for (t in argType.types)
                if (t != "\\false" && t != "\\bool")    // sometimes PhpStorm treats 'false' as 'bool'
                    add(t)
        }
    }

    private fun inferTuple(parameterTypes: List<PhpType>): PhpType {
        return PhpType().add(
                parameterTypes.joinToString(",", "tuple(", ")") { it.toStringAsNested() }
        )
    }

    private fun inferShape(parameterTypes: List<Pair<String, PhpType>>): PhpType {
        return PhpType().add(
            parameterTypes.joinToString(",", "shape(", ")") { it.first + ":" + it.second.toStringAsNested() }
        )
    }
}
