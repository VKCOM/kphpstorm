<?php

class GuestJsonEncoder {
}

class AdminJsonEncoder {
}

/**
 * @kphp-json rename_policy=none
 * @kphp-json for GuestJsonEncoder rename_policy=camelCase
 * @kphp-json for AdminJsonEncoder rename_policy=camelCase
 */
class A {
  /**
   * @kphp-json float_precision = 3
   * @kphp-json for GuestJsonEncoder float_precision = 5
   * @kphp-json for AdminJsonEncoder float_precision = 2
   */
  public $f1 = 123.4567;
}

class B {
  /**
   * @kphp-json skip = true
   * @kphp-json for AdminEncoder skip=false
   * @kphp-json for AdminEncoder rename=super_aa
   */
  public string $f1 = 'f1a';

  /**
   * @kphp-json for AdminEncoder skip
   * @kphp-json for GuestEncoder rename=super_bb
   */
  public string $f2 = 'f2b';
}

class C {
  /**
   * @kphp-json for JsonEncoder skip = true
   */
  public string $f1 = 'aa';
}

/**
 * @kphp-json fields = f1
 * @kphp-json for AdminEncoder fields = f1
 * @kphp-json for GuestEncoder fields = f2
 *
 * @kphp-json rename_policy=snake_case
 *
 * @property-read $f1
 */
class D {
  public bool $f1 = true;

  public bool $f2 = true;
}

/**
 * @kphp-json for AdminEncoder flatten=0
 */
class E {
  private float $f1 = 11.11;
}
