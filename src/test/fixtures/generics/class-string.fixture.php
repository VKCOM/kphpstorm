<?php
/** @noinspection PhpConditionAlreadyCheckedInspection */
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */

/**
 * @kphp-generic T1, T2
 * @param T1[] $array
 * @param class-string<T2> $class
 * @return T2[]
 */
function filter_is_instance($array, $class) {
    return array_filter($array, fn($el) => is_a($el, $class));;
}

use Classes\Base;
use Classes\Child1;
use Classes\Child2;

/**
 * @return Base[]
 */
function get_children() {
    return [new Child1, new Child2()];
}

"Явные типы"; {
    $base_array = get_children();
    $children1_array = filter_is_instance/*<Base, Child1>*/($base_array, Child1::class);
    expr_type($children1_array, "\Classes\Child1[]");
}

"Неявные типы"; {
    $base_array = get_children();
    $children2_array = filter_is_instance($base_array, Child2::class);
    expr_type($children2_array, "\Classes\Child2[]");
}


