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
 * @kphp-json rename_policy = camelCase
 */
class C {
}

/**
 * @kphp-json float_precision=4
 */
class D {
  /**
   * @kphp-json float_precision = 3
   */
  public $f1 = 123.4567;

  /**
   * @kphp-json float_precision=0
   */
  public $f2 = 987.654;
}

/**
 * @kphp-json skip_if_default=false
 */
class E {
  /**
   * @kphp-json raw_string = true
   */
  public $f1 = "";

  /**
   * @kphp-json array_as_hashmap = false
   */
  public $f2 = [];

  /**
   * @kphp-json required=1
   */
  public $f3 = "";

  /**
   * @kphp-json skip = 0
   */
  public $f4 = 123;
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
 * @kphp-json flatten = true
 */
class F3 {
  /**
   * @kphp-json array_as_hashmap
   */
  public $f1 = [1, 2, 3];
}

class G1 {
  /**
   * @kphp-json rename = new_name
   * @kphp-json skip = 0
   */
  public $f1 = 0;
}

/**
 * @kphp-json flatten=false
 * @kphp-json rename_policy = none
 */
class G2 {
  public $f1 = 0;
}

/**
 * @kphp-json flatten= 0
 */
class G3 {
  public $f1 = 1;

  public $f2 = 2;
}

class H {
  /**
   * @kphp-json skip = encode
   */
  public $f1 = 0;

  /**
   * @kphp-json skip=decode
   */
  public $f2 = 0;
}

class I {
  /**
   * @kphp-json float_precision=1
   * @kphp-json skip
   */
  public $f1 = 1;
}
