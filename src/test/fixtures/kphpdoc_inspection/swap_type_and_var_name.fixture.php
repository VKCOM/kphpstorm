<?php

/**
 * <warning descr="Use @param {type} $a, not @param $a {type}">@param</warning> $a int|false
 */
function f1($a) {}

/**
 * comment
 * <warning descr="Use @param {type} $a, not @param $a {type}">@param</warning> $a int comment
 * <warning descr="Use @param {type} $b, not @param $b {type}">@param</warning> $b string|false comment
 *                                           comment
 * comment
 */
function f2($a, $b) {
    /** <warning descr="Use @var {type} $local1, not @var $local1 {type}">@var</warning> $local1 int comment */
    $local1 = 1;

    /** @var string */
    $local2 = '';
}

/**<warning descr="Use @var {type} $input1, not @var $input1 {type}">@var</warning> $input1 GlobalInput*/
/**@var GlobalInput $input2*/

// comment above doc comment
/**
 * <warning descr="Use @param {type} $i, not @param $i {type}"><weak_warning descr="@param just duplicates type hint">@param</weak_warning></warning> $i int
 */
function f3(int $i = 0) {
}

class D2 {
    /**
     * <warning descr="Use @param {type} $some, not @param $some {type}">@param</warning>    $some    int    comment
     */
    static function f1($some) {}
}
