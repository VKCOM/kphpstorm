<?php

namespace Inherit\Main;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }

/**
 * @kphp-generic T
 */
interface GenericInterface {
  /**
   * @return T
   */
  function interfaceMethod();
}

/**
 * @kphp-generic T1, T2
 */
class GenericClass {
  /**
   * @return T1
   */
  function genericMethod1() {}

  /**
   * @return T2
   */
  function genericMethod2() {}

  /**
   * @return T2
   */
  function genericMethod3() {}

  /**
   * @return T2
   */
  function genericMethod4() {}
}

/**
 * @kphp-generic T3, T4
 * @kphp-inherit GenericClass<T3, T4>, GenericInterface<T3>
 */
class FooGeneric extends GenericClass implements GenericInterface {
  /**
   * @inheritDoc
   */
  function interfaceMethod() { return new Foo(); }

  function genericMethod3() {
    echo "1";
    return parent::genericMethod3(); // All reified
  }

  /**
   * @return T3
   */
  function genericMethod4() {
    echo "1";
    return parent::genericMethod4(); // All reified
  }
}

$b = new FooGeneric/*<Foo, Boo>*/();

expr_type($b->interfaceMethod(), "\Inherit\Main\Foo");
expr_type($b->genericMethod1(), "\Inherit\Main\Foo");
expr_type($b->genericMethod2(), "\Inherit\Main\Boo");
expr_type($b->genericMethod3(), "\Inherit\Main\Boo");
expr_type($b->genericMethod4(), "\Inherit\Main\Foo");
