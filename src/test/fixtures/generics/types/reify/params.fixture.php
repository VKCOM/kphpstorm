<?php

interface FuncI {
  function foo();
}

/**
 * @kphp-generic T1: FuncI, T2 = FuncI, T3
 * @param T1 $a
 * @param T2 $b
 * @param T3 $c
 */
function f1($a, $b, $c) {
  expr_type($a, "\FuncI");
  expr_type($b, "\FuncI");

  $a = 100;
  $b = 100;
  $c = 100;

  expr_type($a, "int");
  expr_type($b, "int");
  expr_type($c, "int");
}

/**
 * @kphp-generic T1: callable, T2 = string
 * @param T1 $a
 * @param T2 $b
 * @return T1|T2
 */
function f2($a, $b) {
  expr_type($a, "callable");
  expr_type($b, "string");

  return $a;
}

$a = f2(function() {}, 100);

/**
 * @kphp-generic T: Serializable
 */
class SomeGenericClass {
  /**
   * @param T $el
   */
  public function __construct($el) {
    expr_type($el, "\Serializable");
  }
}
