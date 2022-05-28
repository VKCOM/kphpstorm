<?php


interface FuncI {
    function foo();
}

/**
 * @kphp-generic T1 = FuncI, T2: FuncI, T3
 * @param T1 $a
 * @param T2 $b
 * @param T3 $c
 */
function f1($a, $b, $c) {
    expr_type($a, "\FuncI");
    expr_type($b, "\FuncI");
    expr_type($c, "null");

    $a = 100;
    $b = 100;
    $c = 100;

    expr_type($a, "int");
    expr_type($b, "int");
    expr_type($c, "int");
}

/**
 * @kphp-generic T1 = string, T2: callable
 * @param T1 $a
 * @param T2 $b
 */
function f2($a, $b) {
    expr_type($a, "string");
    expr_type($b, "callable");
}
