<?php

use VK\API\Builders\MsgBuilder;

function withIncorrectParam(int $s, $i) {
}

/**
 * @param int ...$ints
 */
function withVarargParam1(int $s, ...$ints) {
}

/**
 * @param int[] $ints
 */
function withVarargParam2(int $s, ...$ints) {
}

/**
 * @param int|false $i_f
 */
function withDuplicatingParam1(int $s, $i_f) {
}

function withDuplicatingParam2(int $s, int $i_f, string $s_n) {
}

/**
 * @param int[] $arr
 */
function withDuplicatingParam3(int $s, array $arr) {
}

/**
 * @param int $i comment
 * @param string $s comment
 */
function withDuplicatingParam4(int $i, string $s) {
}

function withDuplicatingParam5(int $i, ?string $s) {
}

/**
 * before
 * @param string $s
 * this is treated as comment for $s
 */
function withDuplicatingParam6(int $i, string $s) {
}

/**
 * @param ?A $a
 */
function withDuplicatingParam7(A $a) {
}

/**
 * @param int[] $i_arr
 * @param int|false $i_f
 */
function withTransformToTypeHints1(int $i, string $s, $i_arr, $i_f) {
}

/**
 * @param int[] $i_arr
 * @param int|false $i_f
 */
function withTransformToTypeHints2(int $i, string $s, $i_arr, $i_f) {
}

/**
 * @param int $i comment
 * @param string $s comment
 * @param int[] $i_arr comment
 * @param int|false $i_f comment
 */
function withNoTransformToTypeHints3($i, $s, $i_arr, $i_f) {
}

/**
 * @param ?int[] $i_arr_null
 * @param int|string $complex
 * @param int|false $i_f
 */
function withTransformToTypeHints4(A $a, ?A $a_null, MsgBuilder $builder, ?MsgBuilder $builder_full, \VK\API\Builders\AnotherBuilder $builder_another, ?string $s_null, $i_arr_null, $complex, $i_f) {
}

/**
 * comment
 * @param MsgBuilder $builder comment
 */
function withTransformToTypeHints5(int $i, ?string $s_null, $builder) {
}

function withTransformToTypeHints6(int $i, ?string $s_null) {
}

/**
 * @param int ...$ints
 */
function withTransformToTypeHints8(...$ints) {
}

function lambdaInside() {
    /**
     * @param int $s
     */
    $f1 = function($s, $i) {
    };

    $f2 = /**
     * @param int $s
     */
    function($s, $i) {
    };

    /**
     * @param int $s
     */
    function($s, $i) {
    };
}
