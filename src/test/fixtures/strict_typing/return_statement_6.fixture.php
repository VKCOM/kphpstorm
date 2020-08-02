<?php

/** @return int[] */
function variad1(int ...$args) {
    return $args;
}


function variad2(int ...$args) : array {
    return $args;
}

function variad3(int ...$args) : int {
    <error descr="Can't return 'int[]', expected 'int'">return $args;</error>
}

function variad4(int ...$args) : float {
    return $args[0];
}

/** @return (int|null)[] */
function variad5(?int ...$args) {
    return $args;
}

/** <error descr="Actual return type is 'null[]|int[]'">@return</error> int[] */
function variad6(?int ...$args) {
    <error descr="Can't return 'null[]|int[]', expected 'int[]'">return $args;</error>
}

/**
  * @param int ...$args
  * @return (int|null)[]
  */
function variad7(...$args) {
    return $args;
}

