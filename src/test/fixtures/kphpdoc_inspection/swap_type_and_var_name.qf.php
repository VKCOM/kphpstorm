<?php

/**
 * @param int|false $a
 */
function f1($a) {}

/**
 * comment
 * @param int $a comment
 * @param string|false $b comment
 *                                           comment
 * comment
 */
function f2($a, $b) {
    /** @var int $local1 comment */
    $local1 = 1;

    /** @var string */
    $local2 = '';
}

/**@var GlobalInput $input1 */
/**@var GlobalInput $input2*/

// comment above doc comment
function f3(int $i = 0) {
}

class D2 {
    /**
     * @param    int    $some comment
     */
    static function f1($some) {}
}
