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

$a = mirror(tuple([new GlobalA()], new \Classes\A() ?? new \Classes\C));
expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");


