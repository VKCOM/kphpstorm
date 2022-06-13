<?php

namespace Inherit\SimpleClass;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }

/**
 * @kphp-generic T
 */
class OtherGeneric {
  /**
   * @return T
   */
  function genericMethod() {}
}

/**
 * @kphp-generic T
 * @kphp-inherit OtherGeneric<T>
 */
class FooGeneric extends OtherGeneric {}

$a = new FooGeneric/*<Foo>*/();

expr_type($a->genericMethod(), "\Inherit\SimpleClass\Foo");


/**
 * @kphp-generic T
 * @kphp-inherit OtherGeneric<T|Boo>
 */
class FooGeneric2 extends OtherGeneric {}

$a = new FooGeneric2/*<Foo>*/();

expr_type($a->genericMethod(), "\Inherit\SimpleClass\Boo|\Inherit\SimpleClass\Foo");
