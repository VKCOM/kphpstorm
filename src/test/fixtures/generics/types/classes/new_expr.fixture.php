<?php

class TestA {}

/** @kphp-generic T */
class WithoutConstructor {}

$obj = new WithoutConstructor/*<?TestA>*/();
expr_type($obj, "\WithoutConstructor(?\TestA)");


/** @kphp-generic T */
class OneArgumentConstructor {
  /** @param T $a */
  public function __construct($a) {}
}

$obj = new OneArgumentConstructor/*<?TestA>*/(null);
expr_type($obj, "\OneArgumentConstructor(?\TestA)");

$obj = new OneArgumentConstructor(new TestA);
expr_type($obj, "\OneArgumentConstructor(\TestA)");


/** @kphp-generic T1, T2 */
class TwoArgumentConstructor {
  /**
   * @param T1 $a
   * @param T2 $b
   */
  public function __construct($a, $b) {}
}

$obj1 = new TwoArgumentConstructor(new TestA, "Hello World");
expr_type($obj1, "\TwoArgumentConstructor(\TestA,string)");


/** @kphp-generic T1, T2 */
class TwoGenericTypes {}

$obj = new TwoGenericTypes/*<?TestA, string>*/();
expr_type($obj, "\TwoGenericTypes(?\TestA,string)");
$obj = new TwoGenericTypes/*<?TestA, tuple(int, string)>*/();
expr_type($obj, "\TwoGenericTypes(?\TestA,tuple(int,string))");


/** @kphp-generic T1, T2, T3 */
class ThreeGenericTypes {}

$obj = new ThreeGenericTypes/*<?TestA, string, int>*/();
expr_type($obj, "\ThreeGenericTypes(?\TestA,string,int)");
