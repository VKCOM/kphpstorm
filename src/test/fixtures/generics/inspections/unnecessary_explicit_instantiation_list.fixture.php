<?php

use Classes\Base;
use Classes\Child1;
use Classes\Child2;

// TODO: move function to .meta/functions.php
/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror_2($arg) {
  return $arg;
}

/**
 * @kphp-generic T1, T2
 * @param T1 $a1
 * @param T2 $a2
 * @return T1|T2
 */
function combine_2($a1, $a2) {
  if (0) {
    return $a2;
  }
  return $a1;
}

/**
 * @kphp-generic T1, T2
 * @param T1[]             $array
 * @param class-string<T2> $class
 * @return T2[]
 */
function filter_is_instance_2($array, $class) {
  return array_filter($array, fn($el) => is_a($el, $class));;
}

/**
 * @return Base[]
 */
function get_children_2() {
  return [new Child1, new Child2()];
}

// TODO: fix it
//$a = mirror/*<?GlobalA>*/ (new GlobalA());
//expr_type($a, "\GlobalA|null");

$a = mirror_2<warning descr="Remove unnecessary explicit list of instantiation arguments">/*<string>*/</warning>("");
$a = mirror_2<warning descr="Remove unnecessary explicit list of instantiation arguments">/*<GlobalA[]>*/</warning> ([new GlobalA()]);
$a = combine_2<warning descr="Remove unnecessary explicit list of instantiation arguments">/*<string, int>*/</warning>("", 1);
// TODO: fix it
//$children1_array = filter_is_instance_2/*<Base, Child1>*/(get_children(), Child1::class);

$a1 = mirror_2<warning descr="Remove unnecessary explicit list of instantiation arguments">/*<shape(key1: tuple(GlobalA[], \Classes\A|GlobalC), key2: ?\GlobalD[])>*/</warning>(shape(["key1" => $a, "key2" => [new \GlobalD()]]));
