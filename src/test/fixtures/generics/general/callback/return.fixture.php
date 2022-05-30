<?php

namespace Callback\Return;

class Foo {}

/** @kphp-generic T */
class GenericClass {
  function genericClassMethod(): ?int {}
}

/**
 * @kphp-generic T1, T2
 * @param T1[] $arr
 * @param callable(T1): T2 $fn
 * @return T2[]
 */
function map($arr, $fn) {
  return array_map($fn, $arr);
}


$d = map([""], function($el) {});
expr_type($d, "void[]");


// Work for simple cases.
$a = map([""], function($el) {
  return (int)$el;
});
expr_type($a, "int[]");

$b = map([""], function($el): int {
  return (int)$el;
});
expr_type($b, "int[]");


// Work for simple cases.
$c = map([""], function($el) {
  return new Foo;
});
expr_type($c, "\Callback\Return\Foo[]");

$c = map([""], function($el): Foo {
  return new Foo;
});
expr_type($c, "\Callback\Return\Foo[]");


$c1 = map([""],
  /**
   * @return GenericClass<Foo> $el
   */
  function($el) {
    return new GenericClass;
  }
);

expr_type($c1[0]->genericClassMethod(), "?int");
expr_type($c1, "\Callback\Return\GenericClass(\Callback\Return\Foo)[]|\Callback\Return\GenericClass[]");


// Work for simple cases.
$d = map([""], fn($el) => new Foo);
expr_type($d, "\Callback\Return\Foo[]");

$e = map([""], fn($el): Foo => new Foo);
expr_type($e, "\Callback\Return\Foo[]");

