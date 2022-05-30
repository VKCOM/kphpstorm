<?php

namespace Callback\Param;

class Foo {}
class Boo {}
class Goo {}
class Doo {}
class Loo {}
class Joo {}

/**
 * @kphp-generic T1, T2
 * @param T1[] $arr
 * @param callable(T2): void $fn
 * @return T2[]
 */
function each($arr, $fn) {
  return [];
}

$a = each/*<string, ?int>*/([""], function ($a) {});
expr_type($a, "int[]|null[]");

$a = each([""], function (string $a) {});
// TODO: здесь почему-то в тестах string считается классом и резолвится в FQN
// expr_type($a, "string[]");

$a = each([""], function (Foo $a) {});
expr_type($a, "\Callback\Param\Foo[]");

$a = each([""], fn(Foo $a) => "");
expr_type($a, "\Callback\Param\Foo[]");

/**
 * @kphp-generic T1, T2, T3, T4, T5, T6
 * @param T1[] $arr
 * @param callable(T2, T3, T4, T5): T6 $fn
 * @return T2|T3|T4|T5|T6
 */
function eachOther($arr, $fn) {
  return $arr[0];
}

$a = eachOther([""], function (Foo $a, Boo $b, Goo $c, Doo $d, Loo $e): Joo { return new Joo; });
expr_type($a, "\Callback\Param\Boo|\Callback\Param\Doo|\Callback\Param\Foo|\Callback\Param\Goo|\Callback\Param\Joo");
