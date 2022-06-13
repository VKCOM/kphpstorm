package com.vk.kphpstorm.generics

import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import kotlin.math.min

/**
 * Encapsulates the logic for extracting generic types from the instantiation list.
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

        val explicitSpecsTypes = explicitSpecsPsi.instantiationTypes()

        explicitSpecs.addAll(explicitSpecsTypes)

        for (i in 0 until min(genericsNames.size, explicitSpecsTypes.size)) {
            specializationNameMap[genericsNames[i].name] = explicitSpecsTypes[i]
        }
    }
}
