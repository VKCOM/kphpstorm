<?php

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror1($arg) {
    return $arg;
}


$a = mirror1/*<GlobalA>*/(new GlobalA());
