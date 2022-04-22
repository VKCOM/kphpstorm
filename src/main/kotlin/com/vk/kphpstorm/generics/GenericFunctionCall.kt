package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericUtil.findInstantiationComment
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import kotlin.math.min

/**
 * Класс инкапсулирующий логику вывода шаблонных типов из параметров функций.
 */
class GenericsReifier(val project: Project) {
    val implicitSpecs = mutableListOf<ExPhpType>()
    val implicitSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitClassSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitSpecializationErrors = mutableMapOf<String, Pair<ExPhpType, ExPhpType>>()

    /**
     * Having a call `f(...)` of a template function `f<T1, T2>(...)`, deduce T1 and T2
     * "auto deducing" for generics arguments is typically called "reification".
     */
    fun reifyAllGenericsT(
        parameters: Array<Parameter>,
        genericTs: List<KphpDocGenericParameterDecl>,
        argumentsTypes: List<ExPhpType?>
    ) {
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

        implicitSpecializationNameMap.putAll(implicitClassSpecializationNameMap)
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
            // если случай paramExType это Vector|Vector<%T> и argExType это Vector|Vector<A>
            val instantiationParamType =
                paramExType.items.find { it is ExPhpTypeTplInstantiation } as ExPhpTypeTplInstantiation?
            if (instantiationParamType != null && argExType is ExPhpTypePipe) {
                val instantiationArgType =
                    argExType.items.find { it is ExPhpTypeTplInstantiation } as ExPhpTypeTplInstantiation?
                if (instantiationArgType != null) {
                    for (i in 0 until min(
                        instantiationArgType.specializationList.size,
                        instantiationParamType.specializationList.size
                    )) {
                        reifyArgumentGenericsT(
                            instantiationArgType.specializationList[i],
                            instantiationParamType.specializationList[i]
                        )
                    }
                }
            }

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

        if (paramExType is ExPhpTypeCallable) {
            if (argExType is ExPhpTypeCallable) {
                if (argExType.returnType != null && paramExType.returnType != null) {
                    reifyArgumentGenericsT(argExType.returnType, paramExType.returnType)
                }
                for (i in 0 until min(argExType.argTypes.size, paramExType.argTypes.size)) {
                    reifyArgumentGenericsT(argExType.argTypes[i], paramExType.argTypes[i])
                }
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
        genericsNames: List<KphpDocGenericParameterDecl>,
        explicitSpecsPsi: GenericInstantiationPsiCommentImpl?
    ) {
        if (explicitSpecsPsi == null) return

        val explicitSpecsTypes = explicitSpecsPsi.instantiationPartsTypes()

        explicitSpecs.addAll(explicitSpecsTypes)

        for (i in 0 until min(genericsNames.size, explicitSpecsTypes.size)) {
            specializationNameMap[genericsNames[i].name] = explicitSpecsTypes[i]
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
class IndexingGenericFunctionCall(
    private val fqn: String,
    private val callArgs: Array<PsiElement>,
    reference: PsiElement,
    private val separator: String = "@@",
) {
    private val explicitSpecsPsi = findInstantiationComment(reference)

    fun pack(): String? {
        val explicitSpecsString = extractExplicitGenericsT().joinToString("$$")
        val callArgsString = argumentsTypes().joinToString("$$") {
            // Это необходимо здесь так как например для выражения [new Boo] тип будет #_\int и \Boo
            // и если мы сохраним его как #_\int|\Boo, то в дальнейшем тип будет "#_\int|\Boo", и
            // этот тип не разрешится верно, поэтому сохраняем типы через стрелочку, таким образом
            // внутри PhpType типы будут также разделены, как были на момент сохранения здесь
            if (it.types.size == 1) {
                it.toString()
            } else {
                it.types.joinToString("→")
            }
        }
        // В случае когда нет информации, то мы не сможем вывести более точный тип
        if (explicitSpecsString.isEmpty() && callArgsString.isEmpty()) {
            return null
        }
        return "${fqn}$separator$explicitSpecsString$separator$callArgsString"
    }

    private fun argumentsTypes(): List<PhpType> {
        return callArgs.filterIsInstance<PhpTypedElement>().map { it.type }
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
abstract class ResolvingGenericBase(val project: Project) {
    abstract var parameters: Array<Parameter>
    abstract var genericTs: List<KphpDocGenericParameterDecl>

    protected lateinit var argumentsTypes: List<ExPhpType>
    protected lateinit var explicitGenericsT: List<ExPhpType>

    private val reifier = GenericsReifier(project)

    fun specialization(): List<ExPhpType> {
        return explicitGenericsT.ifEmpty { reifier.implicitSpecs }
    }

    fun unpack(packedData: String): Boolean {
        if (unpackImpl(packedData)) {
            reifier.reifyAllGenericsT(parameters, genericTs, argumentsTypes)
            return true
        }

        return false
    }

    protected abstract fun unpackImpl(packedData: String): Boolean

    protected fun unpackTypeArray(text: String) = if (text.isNotEmpty())
        text.split("$$").mapNotNull {
            val types = it.split("→")
            val type = PhpType()
            types.forEach { singleType ->
                type.add(singleType)
            }
            type.global(project).toExPhpType()
        }
    else
        emptyList()
}


class ResolvingGenericFunctionCall(project: Project) : ResolvingGenericBase(project) {
    lateinit var function: Function
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>

    override fun unpackImpl(packedData: String): Boolean {
        val firstSeparatorIndex = packedData.indexOf("@@")
        if (firstSeparatorIndex == -1) {
            return false
        }
        val functionName = packedData.substring(0, firstSeparatorIndex)

        val remainingPackedData = packedData.substring(firstSeparatorIndex + "@@".length)
        val secondSeparatorIndex = remainingPackedData.indexOf("@@")
        val explicitGenericsString = remainingPackedData.substring(0, secondSeparatorIndex)
        val argumentsTypesString = remainingPackedData.substring(secondSeparatorIndex + "@@".length)

        function = PhpIndex.getInstance(project).getFunctionsByFQN(functionName).firstOrNull() ?: return false

        genericTs = function.genericNames()
        parameters = function.parameters

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentsTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}

class ResolvingGenericConstructorCall(project: Project) : ResolvingGenericBase(project) {
    var klass: PhpClass? = null
    var method: Method? = null
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>

    override fun unpackImpl(packedData: String): Boolean {
        val firstSeparatorIndex = packedData.indexOf("@CO@")
        if (firstSeparatorIndex == -1) {
            return false
        }
        val fqn = packedData.substring(0, firstSeparatorIndex)

        val remainingPackedData = packedData.substring(firstSeparatorIndex + "@CO@".length)
        val secondSeparatorIndex = remainingPackedData.indexOf("@CO@")
        val secondSepIndex = if (secondSeparatorIndex == -1)
            0
        else
            secondSeparatorIndex
        val explicitGenericsString = remainingPackedData.substring(0, secondSepIndex)
        val argumentsTypesString = remainingPackedData.substring(if (secondSepIndex == 0) 0 else secondSepIndex + "@CO@".length)

        val className = fqn.substring(0, fqn.indexOf("__construct"))

        klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull() ?: return false
        method = klass!!.constructor

        parameters = if (klass!!.constructor != null) klass!!.constructor!!.parameters else emptyArray()
        genericTs = klass!!.genericNames()

        explicitGenericsT = unpackTypeArray(explicitGenericsString)
        argumentsTypes = unpackTypeArray(argumentsTypesString)

        return true
    }
}

class ResolvingGenericMethodCall(project: Project) : ResolvingGenericBase(project) {
    var klass: PhpClass? = null
    var method: Method? = null
    var classGenericType: ExPhpTypeTplInstantiation? = null
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>
    lateinit var classGenericTs: List<KphpDocGenericParameterDecl>

    override fun unpackImpl(packedData: String): Boolean {
        val parts = packedData.split("@MC@")
        if (parts.size != 3) {
            return false
        }

        val fqn = parts[0]

        val classNameTypeString = fqn.substring(1, fqn.indexOf('.'))

        val classType = PhpType().add(classNameTypeString).global(project)
        val parsed = classType.toExPhpType()

        // for IDE we return PhpType "A"|"A<T>", that's why
        // A<A<T>> is resolved as "A"|"A<A/A<T>>", so if pipe — search for instantiation
        val instantiation = when (parsed) {
            is ExPhpTypePipe -> parsed.items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> parsed.inner
            else -> parsed
        } as? ExPhpTypeTplInstantiation ?: return false

        classGenericType = instantiation
        val methodName = fqn.substring(fqn.indexOf('.') + 1, fqn.length)

        klass = PhpIndex.getInstance(project).getClassesByFQN(instantiation.classFqn).firstOrNull() ?: return false
        method = klass!!.findMethodByName(methodName)
        if (method == null) {
            return false
        }

        parameters = method!!.parameters
        genericTs = method!!.genericNames()
        classGenericTs = klass!!.genericNames()

        explicitGenericsT = unpackTypeArray(parts[1])
        argumentsTypes = unpackTypeArray(parts[2])

        return true
    }
}

class GenericConstructorCall(call: NewExpression) : GenericCall(call.project) {
    override val callArgs: Array<PsiElement> = call.parameters
    override val argumentsTypes: List<ExPhpType?> = callArgs
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }
    override val explicitSpecsPsi = findInstantiationComment(call)

    private val klass: PhpClass?
    private val method: Method?

    init {
        val className = call.classReference?.fqn
        klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull()
        val constructor = klass?.constructor

        // Если у класса нет конструктора, то создаем его псевдо версию
        method = constructor ?: createPseudoConstructor(project, klass?.name ?: "Foo")

        init()
    }

    override fun function() = method

    override fun isResolved() = method != null && klass != null

    override fun genericNames(): List<KphpDocGenericParameterDecl> {
        val methodsNames = method?.genericNames() ?: emptyList()
        val classesNames = klass?.genericNames() ?: emptyList()

        return mutableListOf<KphpDocGenericParameterDecl>()
            .apply { addAll(methodsNames) }
            .apply { addAll(classesNames) }
            .toList()
    }

    override fun isGeneric() = genericNames().isNotEmpty()

    override fun toString(): String {
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${klass?.fqn ?: "UnknownClass"}->__construct<$explicit>($implicit)"
    }

    private fun createPseudoConstructor(project: Project, className: String): Method {
        return PhpPsiElementFactory.createPhpPsiFromText(
            project,
            Method::class.java, "class $className{ public function __construct() {} }"
        )
    }
}

class GenericMethodCall(call: MethodReference) : GenericCall(call.project) {
    override val callArgs: Array<PsiElement> = call.parameters
    override val argumentsTypes: List<ExPhpType?> = callArgs
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }
    override val explicitSpecsPsi = findInstantiationComment(call)

    private val klass: PhpClass?
    private val method: Method?

    init {
        method = call.resolve() as? Method
        klass = method?.containingClass

        val callType = call.classReference?.type?.global(project)

        val classType = PhpType().add(callType).global(project)
        val parsed = classType.toExPhpType()

        // for IDE we return PhpType "A"|"A<T>", that's why
        // A<A<T>> is resolved as "A"|"A<A/A<T>>", so if pipe — search for instantiation
        val instantiation = when (parsed) {
            is ExPhpTypePipe -> parsed.items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> parsed.inner
            else -> parsed
        } as? ExPhpTypeTplInstantiation

        if (instantiation != null) {
            val specialization = instantiation.specializationList
            val classSpecializationNameMap = mutableMapOf<String, ExPhpType>()
            val genericNames = klass?.genericNames() ?: emptyList()

            for (i in 0 until min(genericNames.size, specialization.size)) {
                classSpecializationNameMap[genericNames[i].name] = specialization[i]
            }

            classSpecializationNameMap.forEach { (name, type) ->
                reifier.implicitClassSpecializationNameMap[name] = type
            }
        }

        init()
    }

    override fun function() = method

    override fun isResolved() = method != null && klass != null

    override fun genericNames(): List<KphpDocGenericParameterDecl> {
        val methodsNames = method?.genericNames() ?: emptyList()
        val classesNames = klass?.genericNames() ?: emptyList()

        return mutableListOf<KphpDocGenericParameterDecl>()
            .apply { addAll(methodsNames) }
            .apply { addAll(classesNames) }
            .toList()
    }

    override fun isGeneric() = genericNames().isNotEmpty()

    override fun toString(): String {
        val function = function()
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${klass?.fqn ?: "UnknownClass"}->${function?.name ?: "UnknownMethod"}<$explicit>($implicit)"
    }
}

class GenericFunctionCall(call: FunctionReference) : GenericCall(call.project) {
    override val callArgs: Array<PsiElement> = call.parameters
    override val argumentsTypes: List<ExPhpType?> = callArgs
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }
    override val explicitSpecsPsi = findInstantiationComment(call)

    private val function: Function? = call.resolve() as? Function

    init {
        init()
    }

    override fun function() = function

    override fun isResolved() = function != null

    override fun genericNames() = function?.genericNames() ?: emptyList()

    override fun isGeneric() = function()?.isGeneric() == true

    override fun toString(): String {
        val function = function()
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${function?.fqn ?: "UnknownFunction"}<$explicit>($implicit)"
    }
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
abstract class GenericCall(val project: Project) {
    abstract val callArgs: Array<PsiElement>
    abstract val explicitSpecsPsi: GenericInstantiationPsiCommentImpl?
    abstract val argumentsTypes: List<ExPhpType?>

    abstract fun function(): Function?
    abstract fun isResolved(): Boolean
    abstract fun genericNames(): List<KphpDocGenericParameterDecl>
    abstract fun isGeneric(): Boolean

    val genericTs = mutableListOf<KphpDocGenericParameterDecl>()
    private val parameters = mutableListOf<Parameter>()

    protected val extractor = GenericInstantiationExtractor()
    protected val reifier = GenericsReifier(project)

    val explicitSpecs get() = extractor.explicitSpecs
    val specializationNameMap get() = extractor.specializationNameMap
    val implicitSpecs get() = reifier.implicitSpecs
    val implicitSpecializationNameMap get() = reifier.implicitSpecializationNameMap
    val implicitClassSpecializationNameMap get() = reifier.implicitClassSpecializationNameMap
    val implicitSpecializationErrors get() = reifier.implicitSpecializationErrors

    protected fun init() {
        val function = function() ?: return
        if (!isGeneric()) return

        val genericNames = genericNames()

        parameters.addAll(function.parameters)
        genericTs.addAll(genericNames)

        // Несмотря на то, что явный список является превалирующим над
        // типами выведенными из аргументов функций, нам все равно
        // необходимы обв списка для дальнейших инспекций

        // В первую очередь, выводим все типы шаблонов из аргументов функции (при наличии)
        reifier.reifyAllGenericsT(function.parameters, genericNames, argumentsTypes)
        // Далее, выводим все типы шаблонов из явного списка типов (при наличии)
        extractor.extractExplicitGenericsT(genericNames(), explicitSpecsPsi)
    }

    fun withExplicitSpecs(): Boolean {
        return explicitSpecsPsi != null
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
        if (explicitSpecsPsi == null) return false

        val countGenericNames = genericNames().size
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
        val function = function() ?: return null

        val param = function.getParameter(index) ?: return null
        val paramType = param.type
        if (paramType.isGeneric(genericNames())) {
            val usedNameMap = extractor.specializationNameMap.ifEmpty {
                reifier.implicitSpecializationNameMap
            }
            return paramType.toExPhpType()?.instantiateGeneric(usedNameMap)
        }

        return null
    }

    abstract override fun toString(): String
}
