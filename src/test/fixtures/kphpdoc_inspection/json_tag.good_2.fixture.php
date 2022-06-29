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
