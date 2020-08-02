<?php

class A {
    /** @var int */
    public $i;

    public static string $s;

    // no phpdoc, not analyzed at all
    public $any = 123;

    /** @var int */
    public $another_i;

    function __construct() {
        // attention! this is error, but PhpStorm after this infers A::$another_id as int|string, as it happens in __construct
        <error descr="Can't assign 'string' to 'int' $another_i">$this->another_i = 's'</error>;
    }
}

function demo0() {
    $a = new A;
    // see __construct()
    <error descr="Can't assign 'int|string' to 'int' $i">$a->i = $a->another_i</error>;
}

function demo() {
    $a = new A;
    $a->i = 4;
    A::$s = 's';

    <error descr="Can't assign 'int[]' to 'int' $i">$a->i = [1]</error>;
    // here 'int|int[]' in error, because $a->i was assigned to 'int[]' just before
    <error descr="Can't assign 'int|int[]' to 'string' $s">A::$s = $a->i</error>;
}

function demo2() {
    $a->any = 123;
    $a->any = 's';
    $a->any = $a;

    A::$s = $a->any;
    $a->any = $a->any;
    $a->i = $a->any;
    A::$s = $a->any;
}

function demo4() {
    $a = new A;
    $a->i = 4;
    A::$s = 's';

    // this error is expected, 'int' to 'string'
    <error descr="Can't assign 'int' to 'string' $s">A::$s = $a->i</error>;
}

