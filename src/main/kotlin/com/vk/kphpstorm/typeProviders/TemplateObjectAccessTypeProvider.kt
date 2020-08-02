package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.impl.FieldReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.NewExpressionImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
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
 */
class TemplateObjectAccessTypeProvider : PhpTypeProvider4 {
    override fun getKey(): Char {
        return 'Щ'
    }

    override fun getType(p: PsiElement): PhpType? {
        // detect type only for $v->p and $v->f(), but NOT for A::$p,
        // because $v::$p is ugly and unsupported (only A::$p), but A::* in case of template classes is useless imho
        // (as we need A<T>::*, I didn't provide such syntax, doesn't make sense)

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
                it.elementType == PhpTokenTypes.C_STYLE_COMMENT &&
                        it.text.startsWith("/*<") &&
                        it.text.endsWith(">*/")
            } ?: return null
            val specTypeStr = specComment.text.substring(3, specComment.text.length - 3)
            val specType = PhpType().add(specTypeStr).toExPhpType() ?: return null
            val classRef = p.classReference ?: return null

//            println("type(new) = ${classRef.fqn}<$specType>")
            return PhpType().add("${classRef.fqn}<$specType>")
        }

        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        // optimization searching for "<" (not to parse otherwise) not done
        val isMethod = incompleteTypeStr[2] == ':'
        val spacePos = incompleteTypeStr.indexOf(' ')
        val memberName = incompleteTypeStr.substring(3, spacePos)
        val lhsTypeStr = incompleteTypeStr.substring(spacePos + 1)

        val lhsType = PhpType().add(lhsTypeStr).global(project)
        val parsed = lhsType.toExPhpType()

        // for IDE we return PhpType "A"|"A<T>", that's why
        // A<A<T>> is resolved as "A"|"A<A/A<T>>", so if pipe — search for instantiation
        val instantiation = when (parsed) {
            is ExPhpTypePipe     -> parsed.items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> parsed.inner
            else                 -> parsed
        } as? ExPhpTypeTplInstantiation ?: return null

        val phpClass = PhpIndex.getInstance(project).getClassesByFQN(instantiation.classFqn).firstOrNull()
                ?: return null
        val docT = phpClass.docComment?.getTagElementsByName("@kphp-template-class")?.firstOrNull() as? KphpDocTagTemplateClassPsiImpl
                ?: return null
        val docTNames = docT.getTemplateArguments()
        val specializationNameMap = mutableMapOf<String, ExPhpType>()
        for (i in 0 until min(docTNames.size, instantiation.specializationList.size))
            specializationNameMap["\\" + docTNames[i]] = instantiation.specializationList[i]

        if (isMethod) {
            val classMethod = phpClass.findMethodByName(memberName) ?: return null
            val methodReturnTag = classMethod.docComment?.returnTag ?: return null
            val methodTypeParsed = methodReturnTag.type.toExPhpType() ?: return null
            val methodTypeSpecialized = methodTypeParsed.instantiateTemplate(specializationNameMap)
//            println("specialized ->$memberName() as $methodTypeSpecialized")

            return methodTypeSpecialized.toPhpType()
        }
        else {
            val classField = phpClass.findFieldByName(memberName, false) ?: return null
            val fieldVarTag = classField.docComment?.varTag ?: return null
            val fieldTypeParsed = fieldVarTag.type.toExPhpType() ?: return null
            val fieldTypeSpecialized = fieldTypeParsed.instantiateTemplate(specializationNameMap)
//            println("specialized ->$memberName as $fieldTypeSpecialized")

            return fieldTypeSpecialized.toPhpType()
        }

    }

    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?): MutableCollection<PhpNamedElement>? {
        return null
    }
}








