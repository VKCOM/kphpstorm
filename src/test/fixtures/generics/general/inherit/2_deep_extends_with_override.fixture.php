<?php

namespace Inherit\TwoDeepExtendsWithOverride;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }

/**
 * @kphp-generic T1, T2
 */
class BaseGeneric {
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
 * @kphp-generic T1, T2
 * @kphp-inherit BaseGeneric<T1, T2>
 */
class FooGeneric extends BaseGeneric {
  /**
   * @return T2
   */
  public function genericMethod1() {}
}

/**
 * @kphp-generic T1, T2
 * @kphp-inherit FooGeneric<T1, T2>
 */
class BooFooGeneric extends FooGeneric {
  /**
   * @return T1
   */
  public function genericMethod2() {}
}

$a = new BooFooGeneric/*<Foo, Boo>*/ ();

expr_type($a->genericMethod1(), "\Inherit\TwoDeepExtendsWithOverride\Boo");
expr_type($a->genericMethod2(), "\Inherit\TwoDeepExtendsWithOverride\Foo");
