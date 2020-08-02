package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.exphptype.KphpPrimitiveTypes

/**
 * 'int', 'bool', 'true', 'var', etc — primitives (not instances!) — psi is just PhpDocType
 * Note! 'mixed' in plain PHP projects will be left 'mixed', in KPHP projects — 'var'.
 * @see KphpPrimitiveTypes
 */
class ExPhpTypePrimitivePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypePrimitive")
    }

    override fun getType(): PhpType {
        val text = this.text

        // Note! For plain PHP projects (not KPHP) leave 'mixed' as is; but in KPHP turn to 'var'.
        // Why? Because native PhpStorm inspections work incorrectly with 'var' (e.g., "no __toString() method")
        // But in KPHP projects — after auto setup is done — all these native inspections are turned off (and KPHPStorm's — on)
        if (text == "mixed") {
            // for plain PHP type inferring remains 'mixed' here; it means, that
            // 1) all native inspections that don't know 'var' work as expected
            // 2) KPHPStorm inspections are by default turned off, if occasionally somebody turns them on —
            //    'mixed' will be treated as 'any' (see PhpTypeToExPhpTypeParsing)
            // 3) type info and quick documentation will show 'mixed': see ExPhpTypeAny::toHumanReadable()
            if (!KphpStormConfiguration.wasSetupForProject(project))
                return PhpType.MIXED
        }

        return KphpPrimitiveTypes.mapPrimitiveToPhpType[text]
                ?: PhpType().add(text)      // not supposed to happen
    }
}
