<?php
/** @noinspection PhpConditionAlreadyCheckedInspection */
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror($arg) {
    return $arg;
}

"Примитивные типы"; {
    $a = mirror("");
    expr_type($a, "string");

    $a1 = mirror(10);
    expr_type($a1, "int");

    $a2 = mirror(true);
    expr_type($a2, "bool");
}


/**
 * @kphp-generic T1, T2
 * @param T1 $a1
 * @param T2 $a2
 * @return T1|T2
 */
function combine($a1, $a2) {
    if (0) return $a2;
    return $a1;
}

"Смешение примитивных типов"; {
    $a = combine("", 1);
    expr_type($a, "int|string");

    $a1 = combine("", true);
    expr_type($a1, "bool|string");

    $a2 = combine("", false);
    expr_type($a2, "false|string");

    $a3 = combine("", true);
    expr_type($a3, "bool|string");

    $a3 = combine("", "");
    expr_type($a3, "string");
}

"Примитивные типы в комплексных"; {
//    $a = combine("", tuple(1, true));
//    expr_type($a, "string|tuple(int,bool)");

//    $a1 = combine([""], shape(["key1" => 1, "key2" => $a]));
//    expr_type($a1, "string[]|shape(key1:int,key2:string)");
}
