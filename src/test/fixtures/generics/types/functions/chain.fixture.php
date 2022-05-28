<?php

namespace Function\Chain;

use Vector;

class Foo {}

/**
 * @kphp-generic T
 * @param T $a
 * @return T
 */
function someGenericFunc($a) { return $a; }

/**
 * @kphp-generic T
 * @param T $a
 * @return T
 */
function someOtherGenericFunc($a) { return $a; }

$a = someGenericFunc(
    someOtherGenericFunc(
        someOtherGenericFunc(
            someGenericFunc(
                someGenericFunc(new Foo())
            )
        )
    )
);

expr_type($a, "\Function\Chain\Foo");

/**
 * @kphp-generic T
 * @param Vector<T> $a
 * @return Vector<T>
 */
function takeVector($a) {
    return $a;
}

$vec = new Vector/*<Foo>*/ ();
$vec2 = takeVector($vec)->get(0);
expr_type($vec2, "\Foo");
