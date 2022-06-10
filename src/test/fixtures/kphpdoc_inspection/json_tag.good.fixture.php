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
  public $f4 = [445];
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
 * @kphp-json fields_visibility=all
 * @kphp-json fields_rename=camelCase
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
}

/**
 * @kphp-json flatten
 */
class E {
  /**
   * @kphp-json float_precision=3
   */
  public $f1 = 123.4567;
}

/**
 * @kphp-json flatten
 */
class F {
  /**
   * @kphp-json array_as_hashmap
   */
  public $f1 = [1, 2, 3];
}
