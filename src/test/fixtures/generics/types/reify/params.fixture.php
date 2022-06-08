<?php

interface FuncI {
  function foo();
}

/**
 * @kphp-generic T1: FuncI, T2, T3 = FuncI
 * @param T1 $a
 * @param T2 $b
 * @param T3 $c
 */
function f1($a, $b, $c) {
  expr_type($a, "\FuncI");
  expr_type($c, "\FuncI");

  $a = 100;
  $b = 100;
  $c = 100;

  expr_type($a, "int");
  expr_type($b, "int");
  expr_type($c, "int");
}

/**
 * @kphp-generic T1: FuncI
 * @param T1[] $a
 * @param ?T1 $b
 * @param T1|int $c
 */
function f2($a, $b, $c) {
  expr_type($a, "\FuncI[]|any[]");
  expr_type($b, "\FuncI|null");
  expr_type($c, "\FuncI|int");
}

/**
 * @kphp-generic T1: callable, T2 = string
 * @param T1 $a
 * @param T2 $b
 * @return T1|T2
 */
function f3($a, $b) {
  expr_type($a, "callable");
  expr_type($b, "string");

  return $a;
}

$a = f3(function() {}, 100);

/**
 * @kphp-generic T: Serializable
 */
class SomeGenericClass {
  /** @var T[] */
  public $data = [];

  /** @var ?T */
  public $data1 = null;

  /** @var T|int */
  public $data2 = 10;

  /**
   * @param T $el
   */
  public function __construct($el) {
    expr_type($el, "\Serializable");

    expr_type($this->data, "\Serializable[]|any[]");
    expr_type($this->data1, "\Serializable|null");
    expr_type($this->data2, "\Serializable|int");
  }
}
