package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.completion.PhpCompletionContributor
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.helpers.KPHP_NATIVE_FUNCTIONS
import com.vk.kphpstorm.kphptags.KphpInferDocTag

/**
 * Reports mismatch in call arguments 'f(...)'.
 *
 * Important! Native inspection "PHP > Type compatibility > Parameter type" must be disabled.
 * @see com.jetbrains.php.lang.inspections.type.PhpParamsInspection
 */
class KphpParameterTypeMismatchInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            override fun visitPhpFunctionCall(reference: FunctionReference) {
                // skip 'use function f'
                if (reference.parent is PhpUse)
                    return
                // we need reference to be unambiguosly resolved, otherwise type checking becomes too complex
                val function = reference.resolve() as? Function ?: return

                checkFuncCall(reference, function, reference.parameters, holder)
            }

            override fun visitPhpMethodReference(reference: MethodReference) {
                // skip 'trait::function as/insteadof rhs' inside detailed trait usage
                if (reference.parent is PhpTraitUseRule)
                    return
                val method = reference.resolve() as? Method ?: return

                checkFuncCall(reference, method, reference.parameters, holder)
            }

            override fun visitPhpNewExpression(expression: NewExpression) {
                // if class has __construct(), reference is resolved to it, not to class itself
                val constructor = expression.classReference?.resolve() as? Method ?: return

                checkFuncCall(expression, constructor, expression.parameters, holder)
            }

        }
    }

    private fun checkFuncCall(call: PhpPsiElement, f: Function, callParams: Array<PsiElement>, holder: ProblemsHolder) {
        val fParams = f.parameters
        val project = holder.project
        val fIsVariadic = f.parameters.isNotEmpty() && f.parameters.last().isVariadic

        loopCallArg@
        for (argIdx in callParams.indices) {
            val callParam = callParams[argIdx]
            val fArg = when {
                argIdx < fParams.size -> fParams[argIdx]
                fIsVariadic           -> f.parameters.last()
                else                  -> break@loopCallArg      // excess parameters are reported by another inspection
            }

            if (PhpCodeInsightUtil.isUnpackedArgument(callParam))
                break@loopCallArg

            val callType = PsiToExPhpType.getTypeOfExpr(callParam, project) ?: continue
            val argType = PsiToExPhpType.getArgumentDeclaredType(fArg, project) ?: continue

            val actualArgType = if (call is FunctionReference) {
                val genericCall = GenericFunctionCall(call)
                if (genericCall.isGeneric())
                    genericCall.typeOfParam(argIdx) ?: argType
                else
                    argType
            } else {
                argType
            }

            if (!actualArgType.isAssignableFrom(callType, project) && needsReporting(f)) {
                holder.registerProblem(callParam, "Can't pass '${callType.toHumanReadable(call)}' to '${actualArgType.toHumanReadable(call)}' \$${fArg.name}", ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
            }
        }

        loopFArg@
        for (argIdx in fParams.indices) {
            val fArg = fParams[argIdx]
            val callParam = when {
                argIdx < callParams.size -> callParams[argIdx]
                else                     -> {
                    if (!fArg.isOptional && !fArg.isVariadic) {
                        val closingBracket = call.lastChild
                        if (closingBracket.elementType == PhpTokenTypes.chRPAREN || closingBracket is FunctionReference || closingBracket is ClassReference)
                            holder.registerProblem(closingBracket, "No value passed for \$${fArg.name}")
                    }
                    break@loopFArg
                }
            }

            if (PhpCodeInsightUtil.isUnpackedArgument(callParam))
                break@loopFArg
        }

    }

    /**
     * If a type mismatch error found — maybe, we don't want to report it, as it could be false positive
     */
    private fun needsReporting(f: Function): Boolean {
        // 1) ignore all from functions.txt — because kphp casts arguments, so passing float is string is ok for example
        if (f !is Method) {
            val funcName = f.name
            if (PhpCompletionContributor.PHP_PREDEFINED_FUNCTIONS.contains(funcName) || KPHP_NATIVE_FUNCTIONS.contains(funcName))
                return false
        }

        // 2) allow passing everything to functions with @kphp-infer cast
        if (f.docComment != null) {
            val tag = KphpInferDocTag.findThisTagInDocComment(f)
            if (tag != null && PhpDocUtil.getTagValue(tag).contains("cast"))
                return false
        }

        return true
    }
}
