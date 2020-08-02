<?php

/**
 * Some comment
 * @see 123
 */
class NoKphpInferClassWithOneMethod {
  private static function method1($foo) {
  }
}

class EmptyClassWithKphpInfer {

    function f1() {}

}

/**
 * @see 123
 * @see 456
 */
function globalF():int { return 0; }

/**
 * @kphp-infer cast
 * @param int[] $arg
 */
function globalFWithCast($arg):int { return 0; }

function withLambda() {
    $f1 = function(int $x) {};
}
