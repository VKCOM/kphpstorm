<?php

class A {
    function aMethod(int $x) {}
}

function getA(): A {
    return new A;
}

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

tp(getA())->aMethod(<error descr="Can't pass 'string' to 'int' $x">'s'</error>);

