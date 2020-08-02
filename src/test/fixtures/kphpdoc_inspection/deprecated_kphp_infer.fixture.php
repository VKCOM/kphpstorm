<?php

/**
 * Some comment
 * @see 123
 */
class NoKphpInferClassWithOneMethod {
  private static function method1($foo) {
  }
}

/**
 * <warning descr="@kphp-infer is deprecated"><warning descr="Tag is not applicable here">@kphp-infer</warning></warning>
 */
class EmptyClassWithKphpInfer {

    /**
     * <warning descr="@kphp-infer is deprecated">@kphp-infer</warning>
     */
    function f1() {}

}

/**
 * @see 123
 * <warning descr="@kphp-infer is deprecated">@kphp-infer</warning>
 * @see 456
 */
function globalF():int { return 0; }

/**
 * @kphp-infer cast
 * @param int[] $arg
 */
function globalFWithCast($arg):int { return 0; }

function withLambda() {
    /**
     * <warning descr="@kphp-infer is deprecated">@kphp-infer</warning>
     */
    $f1 = function(int $x) {};
}
