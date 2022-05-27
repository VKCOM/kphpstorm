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

$a = GenericClass::genericMethod/*<Methods\Main\Foo>*/();
expr_type($a, "\Methods\Main\Foo");


$c = new NonGenericClass();
$d = $c->genericMethod/*<Methods\Main\Foo>*/ ();
expr_type($d, "\Methods\Main\Foo");


$e = NonGenericClass::staticGenericMethod/*<Methods\Main\Foo>*/ ();
expr_type($e, "\Methods\Main\Foo");
