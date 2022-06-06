package com.vk.kphpstorm.exphptype

import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Custom parsing of complex php types — having strings as input, e.g. "tuple<int,A[]>".
 * @see com.vk.kphpstorm.exphptype.psi.TokensToExPhpTypePsiParsing doing the same but tokens->psi.
 * Quite similar to phpdoc.cpp parsing logic in kphp.
 */
object PhpTypeToExPhpTypeParsing {
    private val RE_VALID_FQN = Regex("[a-zA-Z0-9-_\\\\]+")

    /**
     * Pre-cached parsed for primitives and common cases
     * @see KphpPrimitiveTypes.mapPrimitiveToPhpType
     * 2 goals:
     * 1) for primitives, create ExPhpTypePrimitive instead on ExPhpTypeInstance
     * 2) optimization for widely used types and primitive arrays, not to invoke regexp and objects allocation
     */
    private val FQN_PREPARSED = sortedMapOf(
        // synonyms are also listed here, like in mapStringToPhpType
        "int" to ExPhpType.INT,
        "float" to ExPhpType.FLOAT,
        "double" to ExPhpType.FLOAT,
        "string" to ExPhpType.STRING,
        "bool" to ExPhpType.BOOL,
        "boolean" to ExPhpType.BOOL,
        "true" to ExPhpType.BOOL,
        "false" to ExPhpType.FALSE,
        "null" to ExPhpType.NULL,
        "object" to ExPhpType.OBJECT,
        "callable" to ExPhpType.CALLABLE,
        "Closure" to ExPhpType.CALLABLE,
        "void" to ExPhpType.VOID,
        "resource" to ExPhpType.INT,
        "kmixed" to ExPhpType.KMIXED,

        // PhpType().add("int") — types[0] is "\int", so we need everything with leading slash
        "\\int" to ExPhpType.INT,
        "\\float" to ExPhpType.FLOAT,
        "\\double" to ExPhpType.FLOAT,
        "\\string" to ExPhpType.STRING,
        "\\bool" to ExPhpType.BOOL,
        "\\boolean" to ExPhpType.BOOL,
        "\\true" to ExPhpType.BOOL,
        "\\false" to ExPhpType.FALSE,
        "\\null" to ExPhpType.NULL,
        "\\object" to ExPhpType.OBJECT,
        "\\callable" to ExPhpType.CALLABLE,
        "\\void" to ExPhpType.VOID,
        "\\resource" to ExPhpType.INT,
        "\\kmixed" to ExPhpType.KMIXED,

        // arrays of primitives also meet quite often, make them preparsed
        "int[]" to ExPhpTypeArray(ExPhpType.INT),
        "float[]" to ExPhpTypeArray(ExPhpType.FLOAT),
        "string[]" to ExPhpTypeArray(ExPhpType.STRING),
        "bool[]" to ExPhpTypeArray(ExPhpType.BOOL),
        "false[]" to ExPhpTypeArray(ExPhpType.FALSE),
        "null[]" to ExPhpTypeArray(ExPhpType.NULL),
        "object[]" to ExPhpTypeArray(ExPhpType.OBJECT),
        "callable[]" to ExPhpTypeArray(ExPhpType.CALLABLE),
        "kmixed[]" to ExPhpTypeArray(ExPhpType.KMIXED),

        // same arrays of primitives with leading slash
        "\\int[]" to ExPhpTypeArray(ExPhpType.INT),
        "\\float[]" to ExPhpTypeArray(ExPhpType.FLOAT),
        "\\string[]" to ExPhpTypeArray(ExPhpType.STRING),
        "\\bool[]" to ExPhpTypeArray(ExPhpType.BOOL),
        "\\false[]" to ExPhpTypeArray(ExPhpType.FALSE),
        "\\null[]" to ExPhpTypeArray(ExPhpType.NULL),
        "\\object[]" to ExPhpTypeArray(ExPhpType.OBJECT),
        "\\callable[]" to ExPhpTypeArray(ExPhpType.CALLABLE),
        "\\kmixed[]" to ExPhpTypeArray(ExPhpType.KMIXED),

        // 'any' from phpdoc has a special instantiation
        "any" to ExPhpType.ANY,
        "\\any" to ExPhpType.ANY,

        // "array" from phpdoc and "array" emerged by PhpStorm internals is "any[]"
        "array" to ExPhpType.ARRAY_OF_ANY,
        "\\array" to ExPhpType.ARRAY_OF_ANY,
        "any[]" to ExPhpType.ARRAY_OF_ANY,
        "\\any[]" to ExPhpType.ARRAY_OF_ANY,

        // Important!
        // 'mixed' in phpdoc is treated as 'kmixed', that's why
        // 'mixed' can emerge only by PhpStorm internal inferring, when it couldn't detect the type or types are really mixed
        // (for example, [1,'2'] is mixed[] and [new A, new ADevired] is mixed[] in native PhpStorm inferring)
        // so, if PhpStorm couldn't detect, it can be really anything,
        // and we don't have any reason to produce errors and assume whether it is compatible with mixed or not
        "mixed" to ExPhpType.ANY,
        "\\mixed" to ExPhpType.ANY,

        // some "forced" that can occur often, @see [ForcingTypeProvider], \\ not needed
        "force(string)" to ExPhpTypeForcing(ExPhpType.STRING),
        "force(int)" to ExPhpTypeForcing(ExPhpType.INT),
        "force(kmixed)" to ExPhpTypeForcing(ExPhpType.KMIXED),
        "force(any)" to ExPhpTypeForcing(ExPhpType.ANY)
    )

    private class ExPhpTypeBuilder(private val type: String) {
        private var offset = 0

        private fun skipWhitespace() {
            while (offset < type.length && type[offset] == ' ')
                offset++
        }

        fun compare(c: Char): Boolean {
            skipWhitespace()
            return offset < type.length && type[offset] == c
        }

        fun compareAndEat(c: Char): Boolean {
            val eq = compare(c)
            if (eq)
                offset++
            return eq
        }

        fun parseFQN(): String? {
            skipWhitespace()
            val cur = if (offset < type.length) type[offset] else '\b'
            if (!cur.isLetterOrDigit() && cur != '\\' && cur != '-')
                return null
            val match = RE_VALID_FQN.find(type, offset) ?: return null
            offset = match.range.last + 1
            return match.value
        }
    }


    private fun parseTupleContents(builder: ExPhpTypeBuilder): List<ExPhpType>? {
        if (!builder.compareAndEat('(') && !builder.compareAndEat('<'))
            return null
        if (builder.compareAndEat(')') || builder.compareAndEat('>'))
            return listOf()

        val items = mutableListOf<ExPhpType>()
        while (true) {
            items.add(parseTypeExpression(builder) ?: return null)
            if (builder.compareAndEat(')') || builder.compareAndEat('>'))
                return items

            if (builder.compareAndEat(','))
                continue
            return null
        }
    }

    private fun parseShapeContents(builder: ExPhpTypeBuilder): List<ExPhpTypeShape.ShapeItem>? {
        if (!builder.compareAndEat('(') && !builder.compareAndEat('<'))
            return null
        if (builder.compareAndEat(')') || builder.compareAndEat('>'))
            return listOf()

        val items = mutableListOf<ExPhpTypeShape.ShapeItem>()
        while (true) {
            val keyName = builder.parseFQN() ?: return null
            val nullable = builder.compareAndEat('?')
            builder.compareAndEat(':')
            val type = parseTypeExpression(builder) ?: return null

            items.add(ExPhpTypeShape.ShapeItem(keyName, nullable, type))
            if (builder.compareAndEat(')') || builder.compareAndEat('>'))
                return items

            if (builder.compareAndEat(',')) {
                if (builder.compareAndEat('.') && builder.compareAndEat('.') && builder.compareAndEat('.')) {
                    if (!builder.compareAndEat(')') && !builder.compareAndEat('>'))
                        return null
                    return items
                }
                continue
            }
            return null
        }
    }

    private fun parseGenericSpecialization(builder: ExPhpTypeBuilder): List<ExPhpType>? {
        if (!builder.compareAndEat('<') && !builder.compareAndEat('('))
            return null

        val specialization = mutableListOf<ExPhpType>()
        while (true) {
            specialization.add(parseTypeExpression(builder) ?: return null)
            if (builder.compareAndEat('>') || builder.compareAndEat(')'))
                return specialization

            if (builder.compareAndEat(','))
                continue
            return null
        }
    }

    private fun parseTypedCallableContents(builder: ExPhpTypeBuilder): Pair<List<ExPhpType>, ExPhpType?>? {
        if (!builder.compareAndEat('('))
            return null

        val argTypes = mutableListOf<ExPhpType>()
        while (true) {
            if (builder.compareAndEat(')'))
                break
            argTypes.add(parseTypeExpression(builder) ?: return null)

            if (builder.compareAndEat(','))
                continue
            if (!builder.compare(')'))
                return null
        }

        val returnType: ExPhpType? =
            if (builder.compareAndEat(':')) parseTypeExpression(builder) ?: return null
            else null

        return Pair(argTypes, returnType)
    }

    private fun parseClosureTypesListContents(builder: ExPhpTypeBuilder): List<ExPhpType>? {
        val argTypes = mutableListOf<ExPhpType>()

        if (builder.compare(','))
            return argTypes

        while (true) {
            argTypes.add(parseTypeExpression(builder) ?: return null)

            if (builder.compareAndEat('ᤓ'))
                continue

            if (builder.compare(','))
                return argTypes
            if (builder.compare('>'))
                return argTypes
        }
    }

    private fun parseClosureContents(builder: ExPhpTypeBuilder): Pair<List<ExPhpType>, ExPhpType?>? {
        if (!builder.compareAndEat('<'))
            return null

        val argTypes = mutableListOf<ExPhpType>()
        while (true) {
            if (builder.compareAndEat('>'))
                break

            val types = parseClosureTypesListContents(builder) ?: return null
            val type = if (types.isEmpty())
                null
            else if (types.size == 1)
                types[0]
            else
                ExPhpTypePipe(types)

            if (type != null) {
                argTypes.add(type)
            }

            if (builder.compareAndEat(','))
                continue
            if (!builder.compare('>'))
                return null
        }

        val returnType = argTypes.lastOrNull()

        return Pair( argTypes.dropLast(1), returnType)
    }

    private fun parseForcingTypeContents(builder: ExPhpTypeBuilder): ExPhpType? {
        if (!builder.compareAndEat('('))
            return null

        val inner = parseTypeExpression(builder) ?: return null

        if (!builder.compareAndEat(')'))
            return null

        return inner
    }

    private fun parseSimpleType(builder: ExPhpTypeBuilder): ExPhpType? {
        if (builder.compareAndEat('(')) {
            val expr = parseTypeExpression(builder) ?: return null
            builder.compareAndEat(')') || return null
            return expr
        }

        if (builder.compareAndEat('?')) {
            val expr = parseTypeExpression(builder) ?: return null
            return ExPhpTypeNullable(expr)
        }

        if (builder.compareAndEat('%')) {
            val genericsT = builder.parseFQN() ?: return null
            return ExPhpTypeGenericsT(genericsT)
        }

        val fqn = builder.parseFQN() ?: return null

        // TODO: Так как в 2022.2 добилась поддержка типа int<0, 100>, нам нужно
        // вернуть для него всегда просто \int, а не \int<0, 100>
        // Если использовать не typesWithParametrisedParts, а types
        // то это не нужно, но тогда не будет работать вывод типов callable.
        if (fqn == "\\int")
            return FQN_PREPARSED[fqn]

        if (fqn == "tuple" && builder.compare('(')) {
            val items = parseTupleContents(builder) ?: return null
            return ExPhpTypeTuple(items)
        }

        if (fqn == "shape" && builder.compare('(')) {
            val items = parseShapeContents(builder) ?: return null
            return ExPhpTypeShape(items)
        }

        if (fqn == "callable" && builder.compare('(')) {
            val (argTypes, returnType) = parseTypedCallableContents(builder) ?: return null
            return ExPhpTypeCallable(argTypes, returnType)
        }

        if (fqn == "\\Closure" && builder.compare('<')) {
            val (argTypes, returnType) = parseClosureContents(builder) ?: return null
            return ExPhpTypeCallable(argTypes, returnType)
        }

        if (fqn == "force" && builder.compare('(')) {
            val inner = parseForcingTypeContents(builder) ?: return null
            return ExPhpTypeForcing(inner)
        }

        if (fqn == "class-string" && (builder.compare('<') || builder.compare('('))) {
            if (!builder.compareAndEat('(') && !builder.compareAndEat('<'))
                return null

            val genericT = parseSimpleType(builder) ?: return null

            if (genericT !is ExPhpTypeInstance && genericT !is ExPhpTypeGenericsT)
                return null
            if (!builder.compareAndEat(')') && !builder.compareAndEat('>'))
                return null

            return ExPhpTypeClassString(genericT)
        }

        if (builder.compare('<') || builder.compare('(')) {
            val specialization = parseGenericSpecialization(builder) ?: return null
            return ExPhpTypeTplInstantiation(fqn, specialization)
        }

        return FQN_PREPARSED[fqn] ?: ExPhpTypeInstance(fqn)
    }

    private fun parseTypeArray(builder: ExPhpTypeBuilder): ExPhpType? {
        var result = parseSimpleType(builder) ?: return null
        while (builder.compareAndEat('[') && builder.compareAndEat(']')) {
            result = ExPhpTypeArray(result)
        }
        return result
    }

    private fun parseTypeExpression(builder: ExPhpTypeBuilder): ExPhpType? {
        val lhs = parseTypeArray(builder) ?: return null
        // wrap with ExPhpTypePipe only 'T1|T2', leaving 'T' being as is
        if (!builder.compare('|') && !builder.compare('/')) {
            // TODO: здесь была строчка, точно ли она не нужна?
            // return if (lhs is ExPhpTypeForcing) lhs.inner else lhs
            return lhs
        }
        val pipeItems = mutableListOf(lhs)
        while (builder.compareAndEat('|') || builder.compareAndEat('/')) {
            val rhs = parseTypeArray(builder) ?: break
            pipeItems.add(rhs)
        }

        return createPipeOrSimplified(pipeItems)
    }

    /**
     * Having T1|T2|... create ExPhpType representation; not always pipe: int|null will be ?int for example.
     */
    private fun createPipeOrSimplified(pipeItems: List<ExPhpType>): ExPhpType? {
        val size = pipeItems.size

        if (size == 0)
            return null
        if (size == 1)
            return pipeItems[0].let { if (it is ExPhpTypeForcing) it.inner else it }

        // let T|null be ?T
        if (size == 2 && pipeItems[0] === ExPhpType.NULL)
            return createNullableOrSimplified(pipeItems[1])
        if (size == 2 && pipeItems[1] === ExPhpType.NULL)
            return createNullableOrSimplified(pipeItems[0])

        // T1|T2|...|force(T) will be just T
        for (item in pipeItems) {
            if (item is ExPhpTypeForcing) {
                return item.inner
            } else if (item is ExPhpTypeArray && item.inner is ExPhpTypeForcing) {
                // TODO: подумать тут
                return ExPhpTypeArray(item.inner.inner)
            }
        }

        return ExPhpTypePipe(pipeItems)
    }

    private fun createNullableOrSimplified(nullableType: ExPhpType): ExPhpType = when {
        nullableType === ExPhpType.KMIXED -> ExPhpType.KMIXED
        nullableType === ExPhpType.ANY -> ExPhpType.ANY
        nullableType is ExPhpTypeForcing -> nullableType
        else -> ExPhpTypeNullable(nullableType)
    }

    fun parse(phpType: PhpType): ExPhpType? {
        return when (phpType.types.size) {
            0 -> null
            1 ->
//              TODO: здесь не все так просто
                phpType.typesWithParametrisedParts.first().let { str ->
                    FQN_PREPARSED[str] ?: parseTypeExpression(ExPhpTypeBuilder(str))
                }
            else -> {   // optimization: not phpType.toString(), not to concatenate strings
                val items = phpType.types.mapNotNull { str ->
                    // when PhpStorm can't infer, it returns "mixed", for example $tuple[0] is "mixed"|"our_inferred"
                    // so, "mixed" of PhpStorm often messes with our stronger inferring
                    // "mixed" is treated as "any", but in context of pipe, any|int is what we don't need
                    // (to make our inspections work, for example)
                    // so, filter out "mixed" — what PhpStorm couldn't have inferred
                    if (str == "\\mixed")
                        null
                    else
                        FQN_PREPARSED[str] ?: parseTypeExpression(ExPhpTypeBuilder(str))
                }
                createPipeOrSimplified(items)
            }
        }
    }

    fun parseFromString(str: String): ExPhpType? {
        return FQN_PREPARSED[str] ?: parseTypeExpression(ExPhpTypeBuilder(str))
    }
}
