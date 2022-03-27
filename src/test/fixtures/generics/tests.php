<?php
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */


/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function nullable($arg) {
    if (0) return null;
    return $arg;
}

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror($arg) {
    return $arg;
}

$a1 = mirror([new GlobalA()]);
expr_type($a1, "\GlobalA[]");



