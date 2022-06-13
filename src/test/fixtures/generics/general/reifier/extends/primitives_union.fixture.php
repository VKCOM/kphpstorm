<?php

namespace Extends\Reifier\PrimitivesUnion;

/**
 * @kphp-generic T: int|string
 * @param T $a
 * @return T
 */
function takeIntString($a) {
  expr_type($a, "int|string");
  return $a;
}

$a = takeIntString("Hello World");
expr_type($a, "string");

$b = takeIntString(10);
expr_type($b, "int");
