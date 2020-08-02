<?php

/**
 * @param <error descr="Undefined class 'unknown'">unknown</error> $a
 */
function f1($a) {}

/**
 * @param var $a1
 * @param $a2 any
 */
function f2($a1, $a2) {}

class A {
    /** @var future<int> */
    public $i;
    /** @var future_queue<int> */
    public $q;

    /** @var any */
    public $e;
}

/**
 * @return ?tuple(<error descr="Undefined class 'B'">B</error>, A)
 */
function f3() {}

function union_types(int|false $i_f, string|int $s_i, ?callable $callback = null, array|<error descr="Undefined class 'B'">B</error> $f = null) : void {
    new <error descr="Incorrect primitive type usage">int</error>;
}

function invalid_slash(\<error descr="Undefined class 'int'">int</error> $i, \asdf\<error descr="Undefined class 'D'">D</error> $d) {
}
