<?php

namespace Reifier\Generic;

use Vector;
use Pair;

class Foo { function fooMethod() {} }
class Boo { function booMethod() {} }

/**
 * @kphp-generic T1, T2, T3 = Vector<T1|T2>
 * @param T1 $a
 * @param T2 $b
 * @return T3
 */
function foo($a, $b) {
  return $a;
}

$a = foo/*<Foo, Boo>*/(new Foo, new Boo);
expr_type($a, "\Vector|\Vector(\Reifier\Generic\Foo|\Reifier\Generic\Boo)");

$a = foo(new Foo, new Boo);
expr_type($a, "\Vector|\Vector(\Reifier\Generic\Foo|\Reifier\Generic\Boo)");


/**
 * @kphp-generic T1, T2 = Vector<T1>
 * @param T1 $a
 * @return T2
 */
function foo1($a) {
  return $a;
}

$a = foo1/*<Foo>*/(new Foo);
expr_type($a, "\Vector|\Vector(\Reifier\Generic\Foo)");

$a = foo1(new Foo);
expr_type($a, "\Vector|\Vector(\Reifier\Generic\Foo)");


/**
 * @kphp-generic T1, T2 = Vector<T1>, T3 = Vector<T2>
 * @param T1 $a
 * @return T3
 */
function foo2($a) {
  return $a;
}

$a = foo2(new Foo);
expr_type($a, "\Vector|\Vector(\Vector|\Vector(\Reifier\Generic\Foo))");


/**
 * @kphp-generic T1, T2, T3 = Vector<T1>|Pair<T2, string>
 * @param T1 $a
 * @param T2 $b
 * @return T3
 */
function foo3($a, $b) {
  return $a;
}

$a = foo3(new Foo, new Boo);
expr_type($a, "\Pair|\Pair(\Reifier\Generic\Boo,string)|\Vector|\Vector(\Reifier\Generic\Foo)");

$b = $a->get(0);
expr_type($b, "\Reifier\Generic\Foo");

$b = $a->first();
expr_type($b, "\Reifier\Generic\Boo");

$b = $a->second();
expr_type($b, "string");
