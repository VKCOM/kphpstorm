package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.applyIf
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.impl.*
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagTemplateClassPsiImpl
import kotlin.math.min

/**
 * Experimental!
 * Do not enable it in plugin.xml for production!!! Will hang working IDE.
 * Will be reconsidered later.
 *
 * Support for generics, i.e. template class ItemWrapper, @param ItemWrapper<A>
 * KPHP has no support for them now, it's just experiments with IDE for future.
 *
 * TODO: Переписать
 */
class GenericObjectAccessTypeProvider : PhpTypeProvider4 {
    override fun getKey(): Char {
        return 'Щ'
    }

    override fun getType(p: PsiElement): PhpType? {
        // detect type only for $v->p and $v->f(), but NOT for A::$p,
        // because $v::$p is ugly and unsupported (only A::$p), but A::* in case of template classes is useless imho
        // (as we need A<T>::*, I didn't provide such syntax, doesn't make sense)

        if (p is VariableImpl) {
            val containingFunction = PhpPsiUtil.getParentByCondition<PsiElement>(p) {
                it is Function
            } as? Function

            val docT = containingFunction?.docComment?.getTagElementsByName("@kphp-param")?.firstOrNull()
            if (docT != null) {
                val classGenericName = docT.tagValue.split(" ")[0]
                if (classGenericName.contains("<") && classGenericName.contains(">")) {
//                    val templateParams = classTemplateName.slice(
//                        classTemplateName.indexOf("<") + 1 until classTemplateName.indexOf(">")
//                    )

                    val templateParams = classGenericName.slice(
                        classGenericName.indexOf("<") until classGenericName.length
                    ).replace(" ", "")

                    val lhsType = p.inferredType
                    if (lhsType.isEmpty) {
                        return PhpType().add("$classGenericName in ${containingFunction.fqn}")
                    }

                    val resultType = PhpType()
                    lhsType.types.forEach {
                        resultType.add("#Щ$it$templateParams in ${containingFunction.fqn}")
                    }
                    return resultType
                }
            }
        }

        // $v->p
        if (p is FieldReferenceImpl && !p.isStatic) {
            val propertyName = p.name ?: return null
            val lhs = p.classReference ?: return null
            val lhsType = lhs.type

            // optimization isComplete not done

            val resultType = PhpType()
            lhsType.types.forEach {
                resultType.add("#Щ.$propertyName $it")
            }
//            println("type($lhs) = ${resultType.toString().replace("|", " | ")}")
            return resultType
        }

        // $v->f()
        if (p is MethodReferenceImpl && !p.isStatic) {
            val methodName = p.name ?: return null
            val lhs = p.classReference ?: return null
            val lhsType = lhs.type

            // optimization isComplete not done

            val method = PhpIndex.getInstance(p.project).getClassesByFQN(p.fqn).firstOrNull()

            val docT = method?.docComment?.getTagElementsByName("@kphp-generic")?.firstOrNull()
            val docTNames = docT?.tagValue?.let { listOf(it) } ?: emptyList()

            val paramList =
                when (val element = p.element) {
                    is FunctionReferenceImpl -> element.parameterList
                    is MethodReferenceImpl -> element.parameterList
                    else -> null
                }

            val specializationList = paramList?.parameters?.mapNotNull {
                if (it is PhpTypedElement) {
                    it.type.toExPhpType()
                } else {
                    null
                }
            }

            if (specializationList != null) {
                val specializationNameMap = mutableMapOf<String, ExPhpType>()
                for (i in 0 until min(docTNames.size, specializationList.size))
                    specializationNameMap["\\" + docTNames[i]] = specializationList[i]
            }

            val resultType = PhpType()
            lhsType.types.forEach {
                resultType.add("#Щ:$methodName $it")
            }
//            println("type($lhs) = ${resultType.toString().replace("|", " | ")}")
            return resultType
        }

        // new A/*<...args>*/
        if (p is NewExpressionImpl) {
            // todo there is no psi inside C-style comment for now, just parse from string and suppose in has only 1 arg for demo
            // and this arg, if it's a class, must be fqn, not relative
            // (this will be simplified after having psi in C-style comment)
            val specComment = p.firstPsiChild?.nextSibling?.takeIf {
                it is GenericInstantiationPsiCommentImpl
            } ?: return null
            val specTypeStr = specComment.text.substring(3, specComment.text.length - 3)
            val specTypesStr = specTypeStr.split(",").map { it.trim() }

            val specTypes = specTypesStr.mapNotNull {
                PhpType().add(it).toExPhpType()?.toString()
            }.joinToString(",")

            val classRef = p.classReference ?: return null

//            println("type(new) = ${classRef.fqn}<$specType>")
            return PhpType().add("${classRef.fqn}<$specTypes>")
        }

        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        // optimization searching for "<" (not to parse otherwise) not done
        val isMethod = incompleteTypeStr[2] == ':'
        val spacePos = incompleteTypeStr.indexOf(' ')
        val inPos = incompleteTypeStr.indexOf(" in ").applyIf(incompleteTypeStr.indexOf(" in ") == -1) {
            incompleteTypeStr.length
        }
        val memberName = if (spacePos != -1) incompleteTypeStr.substring(3, spacePos) else ""
        val lhsTypeStr = if (spacePos + 1 < inPos) incompleteTypeStr.substring(spacePos + 1, inPos) else ""

        val lhsType = PhpType().add(lhsTypeStr).global(project)
        val parsed = lhsType.toExPhpType()

        // for IDE we return PhpType "A"|"A<T>", that's why
        // A<A<T>> is resolved as "A"|"A<A/A<T>>", so if pipe — search for instantiation
        val instantiation = when (parsed) {
            is ExPhpTypePipe     -> parsed.items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> parsed.inner
            else                 -> parsed
        } as? ExPhpTypeTplInstantiation ?: return null

        val specializationList = mutableListOf<ExPhpType>()
        specializationList.addAll(instantiation.specializationList)

        // TODO: удалить
        val containsUnresolvedTemplateTypes = specializationList.any {
            it != null
        }

        if (containsUnresolvedTemplateTypes && incompleteTypeStr.contains(" in ")) {
            val containingFunctionName = incompleteTypeStr.substring(inPos + " in ".length)
            val containingFunctions = PhpIndex.getInstance(project).getFunctionsByFQN(containingFunctionName)

            val callParamsTypes = mutableMapOf<Int, PhpType>()

            containingFunctions.forEach {
                val refs = ReferencesSearch.search(it)
                refs.forEach forEachRefs@{ ref ->
                    if (ref !is FunctionReferenceImpl) {
                        return@forEachRefs
                    }

                    val params = ref.parameterList
                    params?.parameters?.forEach { paramType ->
                        if (paramType is PhpTypedElement) {
                            val type = paramType.type.toExPhpType()

                            val instant = when (type) {
                                is ExPhpTypePipe     -> type.items.firstOrNull { it is ExPhpTypeTplInstantiation }
                                is ExPhpTypeNullable -> type.inner
                                else                 -> type
                            } as? ExPhpTypeTplInstantiation

                            instant?.specializationList?.forEachIndexed { index, exPhpType ->
                                val paramTypes = callParamsTypes[index]?.add(exPhpType.toPhpType())
                                    ?: exPhpType.toPhpType()
                                callParamsTypes[index] = paramTypes
                            }
                        }
                    }

                }
            }

            specializationList.forEachIndexed { index, spec ->
//                if (spec is ExPhpTypeUnresolved) {
//                    val typeFromCalls = callParamsTypes[index] ?: return@forEachIndexed
//
//                    specializationList[index] = typeFromCalls.toExPhpType()!!
//                }
            }
        }

        val phpClass = PhpIndex.getInstance(project).getClassesByFQN(instantiation.classFqn).firstOrNull()
                ?: return null
        val docT = phpClass.docComment?.getTagElementsByName("@kphp-template-class")?.firstOrNull() as? KphpDocTagTemplateClassPsiImpl
                ?: return null
        val docTNames = docT.getTemplateArguments()
        val specializationNameMap = mutableMapOf<String, ExPhpType>()
        for (i in 0 until min(docTNames.size, specializationList.size))
            specializationNameMap["\\" + docTNames[i]] = specializationList[i]

        if (isMethod) {
            val classMethod = phpClass.findMethodByName(memberName) ?: return null
            val methodReturnTag = classMethod.docComment?.returnTag ?: return null
            val methodTypeParsed = methodReturnTag.type.toExPhpType() ?: return null
            val methodTypeSpecialized = methodTypeParsed.instantiateGeneric(specializationNameMap)
//            println("specialized ->$memberName() as $methodTypeSpecialized")

            return methodTypeSpecialized.toPhpType()
        }
        else {
            val classField = phpClass.findFieldByName(memberName, false) ?: return null
            val fieldVarTag = classField.docComment?.varTag ?: return null
            val fieldTypeParsed = fieldVarTag.type.toExPhpType() ?: return null
            val fieldTypeSpecialized = fieldTypeParsed.instantiateGeneric(specializationNameMap)
//            println("specialized ->$memberName as $fieldTypeSpecialized")

            return fieldTypeSpecialized.toPhpType()
        }

    }

    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?): MutableCollection<PhpNamedElement>? {
        return null
    }
}








