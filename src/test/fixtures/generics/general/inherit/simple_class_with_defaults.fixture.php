<?php

namespace Inherit\SimpleClassWithDefaults;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }

/**
 * @kphp-generic T1, T2 = Boo
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
 * @kphp-generic T
 * @kphp-inherit OtherGeneric<T>
 */
class FooGeneric extends OtherGeneric {}

$a = new FooGeneric/*<Foo>*/();

expr_type($a->genericMethod1(), "\Inherit\SimpleClassWithDefaults\Foo");
expr_type($a->genericMethod2(), "\Inherit\SimpleClassWithDefaults\Boo");
