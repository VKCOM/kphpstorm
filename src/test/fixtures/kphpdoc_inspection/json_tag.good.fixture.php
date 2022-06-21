<?php

class A {
  /**
   * @kphp-json raw_string
   */
  public $f1 = "str";

  /**
   * @kphp-json array_as_hashmap
   */
  public $f2 = ["1", "2", "3"];

  /**
   * @kphp-json array_as_hashmap
   * @var ?int[] $f3
   */
  public $f3 = [445];

  /**
   * @kphp-json array_as_hashmap
   * @var int[] $f4
   */
  public array $f4 = [445];

  /**
   * @kphp-json array_as_hashmap
   * @var int[]
   */
  public $f5;

  function __construct() {
    $this->f5 = [];
  }
}

/**
 * @kphp-json skip_if_default
 */
class B {
  /**
   * @kphp-json skip_if_default
   */
  public $f1 = 0;

  /**
   * @kphp-json skip
   */
  public $f2 = 0;

  /**
   * @kphp-json required
   */
  public $f3 = 0;

  /**
   * @kphp-json rename=new_name
   */
  public $f4 = 0;
}

/**
 * @kphp-json visibility_policy=all
 * @kphp-json rename_policy=camelCase
 */
class C {
}

/**
 * @kphp-json float_precision=4
 */
class D {
  /**
   * @kphp-json float_precision=3
   */
  public $f1 = 123.4567;

  /**
   * @kphp-json float_precision=0
   */
  public $f2 = 987.654;
}

/**
 * @kphp-json flatten
 */
class F1 {
  /**
   * @kphp-json float_precision=3
   */
  public $f1 = 123.4567;
}

/**
 * @kphp-json flatten
 */
class F2 {
  /**
   * @kphp-json raw_string
   */
  public string $f1;
}

/**
 * @kphp-json flatten
 */
class F3 {
  /**
   * @kphp-json array_as_hashmap
   */
  public $f1 = [1, 2, 3];
}
