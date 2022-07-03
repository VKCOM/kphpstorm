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
