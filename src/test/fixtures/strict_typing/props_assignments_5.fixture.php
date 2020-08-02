<?php

class A {
    public int $i;
    public string $s;
    public ?int $i_null;
    public ?string $s_null;

    /** @var tuple(int,int)[] */
    public $t_int_int_arr = [];
}

/** @return tuple(int, string) */
function getT1() {}

function demo1() {
    $a = new A;
    list($i, $s) = getT1();
    $a->i = $i;
    $a->s = $s;
    $a->i_null = $i;
    $a->s_null = $s;
}

function demo2() {
    /** @var $r \tuple(mixed,int)|null */
    list($p, $t) = $r;
    /** @var A $request */
    $request->i = $t;
}

function demo3() {
    $a = new A;
    $a->t_int_int_arr[] = tuple(1, 3);
}
