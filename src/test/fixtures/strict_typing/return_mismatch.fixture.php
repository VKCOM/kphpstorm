<?php

/**
 * <error descr="Actual return type is 'int'">@return</error> string
 */
function mismatch1() {
    <error descr="Can't return 'int', expected 'string'">return 1;</error>
}

/**
 * <error descr="Actual return type is 'int'">@return</error> string
 */
function mismatch2() {
    <error descr="Can't return 'int', expected 'string'">return 1;</error>
}

/**
 * <error descr="Actual return type is 'int'">@return</error> string
 */
function mismatch3(int $i) {
    <error descr="Can't return 'int', expected 'string'">return 1;</error>
}

/**
 * <error descr="Actual return type is '?string'">@return</error> string
 */
function mismatch4() {
    if(0) <error descr="Can't return 'null', expected 'string'">return null;</error>
    return '';
}

/**
 * <error descr="Actual return type is 'string[]'">@return</error> int[]
 */
function mismatch5(): array {
    $a = ['1','2','3'];
    return $a;
}

/**
 * Undetectable by phpstorm, if infers just 'array'/'mixed', so compatible with anything
 * @return int[]
 */
function noMismatch6() {
    return [1,2,'3'];
}

function withLambdas() {
    $f1 = function(): int {
        <error descr="Can't return 'string', expected 'int'">return 'asdf';</error>
    };

    $f2 = function(): void {
        <error descr="Can't return 'string', expected 'void'">return 'asdf';</error>
    };

    // this is ok, because lambdas are not typed, so if not declared - void is not assumed
    $f3 = function() {
        return 'asdf';
    };

    /**
     * <error descr="Actual return type is 'string'">@return</error> void
     */
    $f4 = function() {
        <error descr="Can't return 'string', expected 'void'">return 'asdf';</error>
    };

    $f5 = function($untyped): int {
        return $untyped;
    };

    $f6 = function($untyped): void {
        return $untyped;
    };
}
