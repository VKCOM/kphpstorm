<?php

namespace Methods\Main;

class Foo {}

/**
 * @kphp-generic T1 = int, T2
 */
class GenericClass {
  /**
   * @kphp-generic T
   * @return T
   */
  static function genericMethod() {}
}

class NonGenericClass {
  /**
   * @kphp-generic T
   * @return T
   */
  function genericMethod() {}

  /**
   * @kphp-generic T
   * @return T
   */
  static function staticGenericMethod() {}
}

$a = GenericClass::genericMethod/*<Foo>*/();
expr_type($a, "\Methods\Main\Foo");


$c = new NonGenericClass();
$d = $c->genericMethod/*<Foo>*/ ();
expr_type($d, "\Methods\Main\Foo");


$e = NonGenericClass::staticGenericMethod/*<Foo>*/ ();
expr_type($e, "\Methods\Main\Foo");
