<?php

namespace Inherit\TwoDeepExtends;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }

class BaseNonGeneric {}

/**
 * @kphp-generic T1, T2
 */
class BaseGeneric extends BaseNonGeneric {
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
class FooGeneric extends BaseGeneric {}

/**
 * @kphp-generic T1, T2
 * @kphp-inherit FooGeneric<T1, T2>
 */
class BooFooGeneric extends FooGeneric {}

$a = new BooFooGeneric/*<Foo, Boo>*/ ();

expr_type($a->genericMethod1(), "\Inherit\TwoDeepExtends\Foo");
expr_type($a->genericMethod2(), "\Inherit\TwoDeepExtends\Boo");


/**
 * @kphp-generic T1, T2
 * @kphp-inherit FooGeneric<T2, T1>
 */
class BooFooReverseGeneric extends FooGeneric {}

$b = new BooFooReverseGeneric/*<Foo, Boo>*/ ();

expr_type($b->genericMethod1(), "\Inherit\TwoDeepExtends\Boo");
expr_type($b->genericMethod2(), "\Inherit\TwoDeepExtends\Foo");


/**
 * @kphp-generic T1, T2
 * @kphp-inherit BaseGeneric<T2, T1>
 */
class FooReverseGeneric extends BaseGeneric {}

/**
 * @kphp-generic T1, T2
 * @kphp-inherit FooReverseGeneric<T2, T1>
 */
class BooFooDoubleReverseGeneric extends FooReverseGeneric {}

$c = new BooFooDoubleReverseGeneric/*<Foo, Boo>*/ ();

expr_type($c->genericMethod1(), "\Inherit\TwoDeepExtends\Foo");
expr_type($c->genericMethod2(), "\Inherit\TwoDeepExtends\Boo");


/**
 * @kphp-generic T3, T4
 * @kphp-inherit BaseGeneric<T3, T4>
 */
class FooOtherTNamesGeneric extends BaseGeneric {}

/**
 * @kphp-generic T5, T6
 * @kphp-inherit FooOtherTNamesGeneric<T5, T6>
 */
class BooFooOtherTNamesGeneric extends FooOtherTNamesGeneric {}

$d = new BooFooOtherTNamesGeneric/*<Foo, Boo>*/ ();

expr_type($d->genericMethod1(), "\Inherit\TwoDeepExtends\Foo");
expr_type($d->genericMethod2(), "\Inherit\TwoDeepExtends\Boo");
