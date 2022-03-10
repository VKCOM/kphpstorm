<?php

class A {}

/**
 * @kphp-template T
 * @param T $o
 * @return T
 */
function tp($o) {
    /** @var T $o2 */
    $o2 = $o;
    (function() use($o) {
        /** @var T[] $o3 */
        $o3 = [$o];
    })();
    return $o2;
}

tp(new A);
