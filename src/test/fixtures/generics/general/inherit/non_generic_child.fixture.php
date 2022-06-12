<?php

namespace Inherit\NonGenericChild;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }

/**
 * @kphp-generic T1, T2
 */
class OtherGeneric {
  /**
   * @return T1
   */
  function genericMethod1() {}

  /**
   * @return T2
   */
  function genericMethod2() {}
}

/**
 * @kphp-inherit OtherGeneric<Foo, Boo>
 */
class FooNonGeneric extends OtherGeneric {}

$a = new FooNonGeneric();

expr_type($a->genericMethod1(), "\Inherit\NonGenericChild\Foo");
expr_type($a->genericMethod2(), "\Inherit\NonGenericChild\Boo");
