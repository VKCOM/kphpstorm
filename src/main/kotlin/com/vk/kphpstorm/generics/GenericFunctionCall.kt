package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericFunctionUtil.genericNames
import com.vk.kphpstorm.generics.GenericFunctionUtil.isGeneric
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.toExPhpType
import kotlin.math.min

/**
 * Класс инкапсулирующий логику вывода шаблонных типов из параметров функций.
 */
class GenericsReifier(val project: Project) {
    val implicitSpecs = mutableListOf<ExPhpType>()
    val implicitSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitSpecializationErrors = mutableMapOf<String, Pair<ExPhpType, ExPhpType>>()

    /**
     * Having a call `f(...)` of a template function `f<T1, T2>(...)`, deduce T1 and T2
     * "auto deducing" for generics arguments is typically called "reification".
     */
    fun reifyAllGenericsT(function: Function, argumentsTypes: List<ExPhpType?>) {
        val genericTs = function.genericNames()
        val parameters = function.parameters

        for (i in 0 until min(argumentsTypes.size, parameters.size)) {
            val param = parameters[i] as? PhpTypedElement ?: continue
            val paramType = param.type.global(project)
            val paramExType = paramType.toExPhpType() ?: continue

            // Если параметр не является шаблонным, то мы его пропускаем
            if (!paramType.isGeneric(genericTs)) {
                continue
            }

            val argExType = argumentsTypes[i] ?: continue
            reifyArgumentGenericsT(argExType, paramExType)
        }
    }

    /**
     * Having a call `f($arg)`, where `f` is `f<T>`, and `$arg` is `@param T[] $array`, deduce T.
     *
     * For example:
     * 1. if `@param T[]` and `$arg` is `A[]`, then T is A
     * 2. if `@param class-string<T>` and `$arg` is `class-string<A>`, then T is A
     * 3. if `@param shape(key: T)` and `$arg` is `shape(key: A)`, then T is A
     *
     * This function is called for every template argument of `f()` invocation.
     *
     * TODO: add callable support
     */
    private fun reifyArgumentGenericsT(argExType: ExPhpType, paramExType: ExPhpType) {
        if (paramExType is ExPhpTypeGenericsT) {
            val prevReifiedType = implicitSpecializationNameMap[paramExType.nameT]
            if (prevReifiedType != null) {
                // В таком случае мы получаем ситуацию когда один шаблонный тип
                // имеет несколько возможных вариантов типа, что является ошибкой.
                implicitSpecializationErrors[paramExType.nameT] = Pair(argExType, prevReifiedType)
            }

            implicitSpecializationNameMap[paramExType.nameT] = argExType
            implicitSpecs.add(argExType)
        }

        if (paramExType is ExPhpTypeNullable) {
            if (argExType is ExPhpTypeNullable) {
                reifyArgumentGenericsT(argExType.inner, paramExType.inner)
            } else {
                reifyArgumentGenericsT(argExType, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypePipe) {
            // если случай T|false
            if (paramExType.items.size == 2 && paramExType.items.any { it == ExPhpType.FALSE }) {
                if (argExType is ExPhpTypePipe) {
                    val argTypeWithoutFalse = ExPhpTypePipe(argExType.items.filter { it != ExPhpType.FALSE })
                    val paramTypeWithoutFalse = paramExType.items.first { it != ExPhpType.FALSE }
                    reifyArgumentGenericsT(argTypeWithoutFalse, paramTypeWithoutFalse)
                }
                // TODO: подумать над пайпами, так как не все так очевидно
            }
        }

        if (paramExType is ExPhpTypeClassString) {
            val isPipeWithClassString = argExType is ExPhpTypePipe &&
                    argExType.items.any { it is ExPhpTypeClassString }

            // Для случаев когда нативный вывод типов дает в результате string|class-string<Boo>
            // В таком случае нам необходимо найти более точный тип.
            val classStringType = if (isPipeWithClassString) {
                (argExType as ExPhpTypePipe).items.find { it is ExPhpTypeClassString }
            } else if (argExType is ExPhpTypeClassString) {
                argExType
            } else {
                null
            }

            if (classStringType is ExPhpTypeClassString) {
                reifyArgumentGenericsT(classStringType.inner, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypeArray) {
            if (argExType is ExPhpTypeArray) {
                reifyArgumentGenericsT(argExType.inner, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypeTuple) {
            if (argExType is ExPhpTypeTuple) {
                for (i in 0 until min(argExType.items.size, paramExType.items.size)) {
                    reifyArgumentGenericsT(argExType.items[i], paramExType.items[i])
                }
            }
        }

        if (paramExType is ExPhpTypeShape) {
            val isPipeWithShapes = argExType is ExPhpTypePipe &&
                    argExType.items.any { it is ExPhpTypeShape && it.items.isNotEmpty() }

            // Для случаев когда нативный вывод типов дает в результате shape()|shape(key1:Foo...)
            // В таком случае нам необходимо вычленить более точный шейп.
            val shapeWithKeys = if (isPipeWithShapes) {
                (argExType as ExPhpTypePipe).items.find { it is ExPhpTypeShape && it.items.isNotEmpty() }
            } else if (argExType is ExPhpTypeShape) {
                argExType
            } else {
                null
            }

            if (shapeWithKeys is ExPhpTypeShape) {
                shapeWithKeys.items.forEach { argShapeItem ->
                    val correspondingParamShapeItem = paramExType.items.find { paramShapeItem ->
                        argShapeItem.keyName == paramShapeItem.keyName
                    } ?: return@forEach

                    reifyArgumentGenericsT(argShapeItem.type, correspondingParamShapeItem.type)
                }
            }
        }
    }
}

/**
 * Класс инкапсулирующий логику выделения шаблонных типов из списка инстанциации.
 */
class GenericInstantiationExtractor {
    val explicitSpecs = mutableListOf<ExPhpType>()
    val specializationNameMap = mutableMapOf<String, ExPhpType>()

    /**
     * Having a call `f/*<A, B>*/(...)`, where `f` is `f<T1, T2>`, deduce T1 and T2 from
     * comment `/*<A, B>*/`.
     */
    fun extractExplicitGenericsT(
        function: Function,
        explicitSpecsPsi: GenericInstantiationPsiCommentImpl?
    ) {
        if (explicitSpecsPsi == null) return

        val explicitSpecsTypes = explicitSpecsPsi.instantiationPartsTypes()

        explicitSpecs.addAll(explicitSpecsTypes)

        val genericTs = function.genericNames()
        for (i in 0 until min(genericTs.size, explicitSpecsTypes.size)) {
            specializationNameMap[genericTs[i]] = explicitSpecsTypes[i]
        }
    }
}

/**
 * Ввиду того, что мы не можем резолвить функции во время вывода типов,
 * нам необходимо выделить всю необходимую информацию для дальнейшего
 * вывода.
 *
 * Таким образом данный класс выделяет типы из явного списка инстанциации
 * и выводит типы аргументов для вызова. Полученные данные пакуются в
 * строку.
 *
 * Полученная строка может быть передана далее в [ResolvingGenericFunctionCall.unpack],
 * для дальнейшей обработки.
 */
class IndexingGenericFunctionCall(val call: FunctionReference) {
    private val fqn = call.fqn
    private val callArgs = call.parameters
    private val explicitSpecsPsi = call.firstChild?.nextSibling?.takeIf {
        it is GenericInstantiationPsiCommentImpl
    } as? GenericInstantiationPsiCommentImpl

    fun pack(): String {
        val explicitSpecsString = extractExplicitGenericsT().joinToString("$$")
        val callArgsString = argumentsTypes().joinToString("$$")
        return "${fqn}@@$explicitSpecsString@@$callArgsString"
    }

    private fun argumentsTypes(): List<ExPhpType?> {
        return callArgs.filterIsInstance<PhpTypedElement>().map { it.type.toExPhpType() }
    }

    private fun extractExplicitGenericsT(): List<ExPhpType> {
        if (explicitSpecsPsi == null) return emptyList()
        return explicitSpecsPsi.instantiationPartsTypes()
    }
}

/**
 * Данный класс инкапсулирует логику обработки данных полученных на этапе
 * индексации и вывода типов ([IndexingGenericFunctionCall]).
 *
 * Результатом для данного класса являются данные возвращаемые методом
 * [specialization], данный метод возвращает список шаблонных типов
 * для данного вызова.
 */
class ResolvingGenericFunctionCall(val project: Project) {
    lateinit var function: Function
    lateinit var genericTs: List<String>
    private lateinit var parameters: Array<Parameter>
    private lateinit var argumentsTypes: List<ExPhpType?>
    private lateinit var explicitGenericsT: List<ExPhpType?>

    private val reifier = GenericsReifier(project)

    fun specialization(): List<ExPhpType?> {
        return explicitGenericsT.ifEmpty { reifier.implicitSpecs }
    }

    fun unpack(packedData: String): Boolean {
        if (unpackImpl(packedData)) {
            reifier.reifyAllGenericsT(function, argumentsTypes)
            return true
        }

        return false
    }

    private fun unpackImpl(packedData: String): Boolean {
        val parts = packedData.split("@@")
        if (parts.size != 3) {
            return false
        }

        val functionName = parts[0]
        function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull() ?: return false

        genericTs = function.genericNames()
        parameters = function.parameters

        explicitGenericsT = unpackTypeArray(parts[1])
        argumentsTypes = unpackTypeArray(parts[2])

        return true
    }

    private fun unpackTypeArray(text: String) = if (text.isNotEmpty())
        text.split("$$").map { PhpType().add(it).toExPhpType() }
    else
        emptyList()
}

/**
 * Ввиду причин описанных в [IndexingGenericFunctionCall], мы не можем использовать
 * объединенный класс для обработки вызова во время вывода типов. Однако в других
 * местах мы можем использовать индекс и поэтому нам не нужно паковать данные и
 * потом их распаковывать, мы можем делать все за раз.
 *
 * Данный класс является объединением [IndexingGenericFunctionCall] и
 * [ResolvingGenericFunctionCall] и может быть использован для обработки шаблонных
 * вызовов в других местах;
 *
 * Класс инкапсулирующий в себе всю логику обработки шаблонных вызовов.
 *
 * Он выводит неявные типы, когда нет явного определения списка типов при инстанциации.
 *
 * Например:
 *
 * ```php
 * /**
 *  * @kphp-generic T1, T2
 *  * @param T1 $a
 *  * @param T2 $b
 *  */
 * function f($a, $b) {}
 *
 * f(new A, new B); // => T1 = A, T2 = B
 * ```
 *
 * В случае когда есть явный список типов при инстанциации, он собирает типы
 * из него.
 *
 * Например:
 *
 * ```php
 * f/*<C, D>*/(new A, new B); // => T1 = C, T2 = D
 * ```
 */
class GenericFunctionCall(call: FunctionReference) {
    val project = call.project
    val function = call.resolve() as? Function
    val genericTs = mutableListOf<String>()
    private val parameters = mutableListOf<Parameter>()
    private val callArgs = call.parameters
    private val argumentsTypes = callArgs.filterIsInstance<PhpTypedElement>().map { it.type.toExPhpType() }

    val explicitSpecsPsi = call.firstChild?.nextSibling?.takeIf {
        it is GenericInstantiationPsiCommentImpl
    } as? GenericInstantiationPsiCommentImpl

    private val extractor = GenericInstantiationExtractor()
    private val reifier = GenericsReifier(call.project)

    val explicitSpecs get() = extractor.explicitSpecs
    val implicitSpecs get() = reifier.implicitSpecs
    val implicitSpecializationNameMap get() = reifier.implicitSpecializationNameMap
    val implicitSpecializationErrors get() = reifier.implicitSpecializationErrors

    init {
        init()
    }

    private fun init() {
        if (function == null || !isGeneric()) return

        parameters.addAll(function.parameters)
        genericTs.addAll(function.genericNames())

        // Несмотря на то, что явный список является превалирующим над
        // типами выведенными из аргументов функций, нам все равно
        // необходимы обв списка для дальнейших инспекций

        // В первую очередь, выводим все типы шаблонов из аргументов функции (при наличии)
        reifier.reifyAllGenericsT(function, argumentsTypes)
        // Далее, выводим все типы шаблонов из явного списка типов (при наличии)
        extractor.extractExplicitGenericsT(function, explicitSpecsPsi)
    }

    fun withExplicitSpecs(): Boolean {
        return explicitSpecsPsi != null
    }

    fun isGeneric(): Boolean {
        return function?.isGeneric() == true
    }

    /**
     * Функция проверяющая, что явно указанные шаблонные типы
     * соответствуют автоматически выведенным типам и их можно
     * безопасно удалить.
     *
     * Например:
     *
     * ```php
     * /**
     *  * @kphp-generic T
     *  * @param T $arg
     *  */
     * function f($arg) {}
     *
     * f/*<Foo>*/(new Foo); // === f(new Foo);
     * ```
     */
    fun isNoNeedExplicitSpec(): Boolean {
        if (function == null) return false
        if (explicitSpecsPsi == null) return false

        val countGenericNames = function.genericNames().size
        val countExplicitSpecs = explicitSpecs.size
        val countImplicitSpecs = implicitSpecs.size

        if (countGenericNames == countExplicitSpecs && countExplicitSpecs == countImplicitSpecs) {
            var isEqual = true
            explicitSpecs.forEachIndexed { index, explicitSpec ->
                if (!implicitSpecs[index].isAssignableFrom(explicitSpec, project)) {
                    isEqual = false
                }
            }
            return isEqual
        }

        return false
    }

    /**
     * Имея следующую функцию:
     *
     * ```php
     * /**
     *  * @kphp-generic T
     *  * @param T $arg
     *  */
     * function f($arg) {}
     * ```
     *
     * И следующий вызов:
     *
     * ```php
     * f/*<Foo>*/(new Foo);
     * ```
     *
     * Нам необходимо вывести тип `$arg`, для того чтобы проверить, что
     * переданное выражение `new Foo` имеет правильный тип.
     *
     * Так как функция может вызываться с разными шаблонными типа, нам
     * необходимо найти тип `$arg` для каждого конкретного вызова.
     * В примере выше в результате будет возвращен тип `Foo`.
     */
    fun typeOfParam(index: Int): ExPhpType? {
        if (function == null) return null

        val param = function.getParameter(index) ?: return null
        val paramType = param.type
        if (paramType.isGeneric(function)) {
            val usedNameMap = extractor.specializationNameMap.ifEmpty {
                reifier.implicitSpecializationNameMap
            }
            return paramType.toExPhpType()?.instantiateGeneric(usedNameMap)
        }

        return null
    }

    override fun toString(): String {
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${function?.fqn ?: "UnknownFunction"}<$explicit>($implicit)"
    }
}
