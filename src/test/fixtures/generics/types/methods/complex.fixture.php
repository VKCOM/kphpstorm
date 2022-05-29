<?php

namespace Methods\Complex;

use pair;
use Vector;

class Goo {}

/**
 * @kphp-generic T
 */
class Foo {
  /**
   * @return T[]
   */
  function getArray(): array {
    return [];
  }
}

$vec = new Vector/*<Pair<Foo<Goo>, Goo>>*/();
$a = $vec->get(0)->first()->getArray()[0];
expr_type($a, "\Methods\Complex\Goo");
