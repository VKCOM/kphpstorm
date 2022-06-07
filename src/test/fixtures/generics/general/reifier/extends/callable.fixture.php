<?php

namespace Extends\Reifier\Callable;

/**
 * @kphp-generic T: callable
 * @param T $a
 * @return T
 */
function takeCallable($a) {
  expr_type($a, "callable");
  $a();
  return $a;
}

$a = takeCallable(function() {});
expr_type($a, "callable");


/**
 * @kphp-generic T: callable(int, string): void
 * @param T $a
 * @return T
 */
function takeTypedCallable($a) {
  expr_type($a, "callable");
  $a();
  return $a;
}

$b = takeTypedCallable(function() {});
expr_type($b, "callable");

$b = takeTypedCallable("");
expr_type($b, "string");
