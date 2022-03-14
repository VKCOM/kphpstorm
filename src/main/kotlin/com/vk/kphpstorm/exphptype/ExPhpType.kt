package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.typeProviders.TupleShapeTypeProvider

/**
 * Purpose:
 * We want to parse complex types like like 'tuple<int, A|null>' in @param/@return/@var
 * and handle them on type inferring
 * (e.g., get()[1]-> is instance of A, should suggest its methods, find usages should work).
 *
 * The concept of realization is:
 * 1)
 * parsing of @return etc is overridden: [com.vk.kphpstorm.KphpStormParserDefinition]
 * psi tree of a type will be a tree of custom psi elements [com.vk.kphpstorm.exphptype.psi]
 * e.g., 'tuple<int, A|null>' is ( tuple ( primitive(int), pipe ( primitive(A), primitive(null) ) )
 * @see com.vk.kphpstorm.exphptype.psi.ExPhpTypeTuplePsiImpl and others in that package
 * 2)
 * each psi element implements [com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType]
 * getType() method is overridden, it returns [PhpType] as strings
 * thus, getType() is called natively of the upper-most psi child inside @return etc
 * and saved into stubs / used in native type inferring
 * e.g., 'tuple<int, A|null>' in PhpType will be ["tuple(int,A/null)"]
 * @see com.vk.kphpstorm.exphptype.psi.ExPhpTypeTuplePsiImpl.getType and others getType()
 * 3)
 * custom type providers
 * for example, get()[1]->... — retreive type of get(); retreiving sees '@return ...',
 * calls getType() from '@return' [com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl.getType],
 * which calls our custom psi getType() or takes from stubs (then it was previously saved there)
 * e.g., for get() we have ["tuple(int,A/null)"] (or incomplete) — remember it and remember '1'
 * @see TupleShapeTypeProvider
 * 4)
 * we know the type of get() — in custom string format, saved into PhpType
 * this string format can be parsed by [PhpTypeToExPhpTypeParsing.parse]
 * e.g., for get() we have [ExPhpTypeTuple] ([ExPhpTypePrimitive] int, [ExPhpTypePipe] A|null)
 * knowing '1', we call [ExPhpType.getSubkeyByIndex] ('1')
 * e.g., for get()[1] we get [ExPhpTypePipe]: A|null
 * @see TupleShapeTypeProvider.complete
 * Finally, calling [toPhpType] we infer type of 'get()[1]' psi node as PhpType ["\A", "null"].
 * Suggestions of props/methods of class A and find usages work.
 *
 * Unlike [com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes],
 * no need to use <stubElementTypeHolder>, as custom psi elementTypes are not stubbed.
 */
interface ExPhpType {
    fun toPhpType(): PhpType
    fun toHumanReadable(expr: PhpPsiElement): String
    fun getSubkeyByIndex(indexKey: String): ExPhpType?
    fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType
    fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean

    companion object {
        val INT = ExPhpTypePrimitive(KphpPrimitiveTypes.INT)
        val FLOAT = ExPhpTypePrimitive(KphpPrimitiveTypes.FLOAT)
        val STRING = ExPhpTypePrimitive(KphpPrimitiveTypes.STRING)
        val BOOL = ExPhpTypePrimitive(KphpPrimitiveTypes.BOOL)
        val FALSE = ExPhpTypePrimitive(KphpPrimitiveTypes.FALSE)
        val NULL = ExPhpTypePrimitive(KphpPrimitiveTypes.NULL)
        val OBJECT = ExPhpTypePrimitive(KphpPrimitiveTypes.OBJECT)
        val CALLABLE = ExPhpTypePrimitive(KphpPrimitiveTypes.CALLABLE)
        val VOID = ExPhpTypePrimitive(KphpPrimitiveTypes.VOID)
        val KMIXED = ExPhpTypePrimitive(KphpPrimitiveTypes.KMIXED)

        val ANY = ExPhpTypeAny()
        val ARRAY_OF_ANY = ExPhpTypeArray(ANY)
    }
}

