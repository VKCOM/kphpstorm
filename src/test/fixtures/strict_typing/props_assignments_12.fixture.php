<?php

function tuple(...$args) {
    return ${'args'};
}

function shape(array $associative_arr) {
    return ${'associative_arr'};
}

class A {
    /** @var tuple(int, string) */
    public $t_int_string;
    /** @var tuple(int, A) */
    public $t_int_A;
    /** @var shape(i:int, a:A, ...) */
    public $s_int_a;
}

/** @return tuple(int, A) */
function getTupleIntA() {
}

function demo1() {
    $a1 = new A;
    $a2 = new A;
    <error descr="Can't assign 'tuple(int, A)' to 'tuple(int, string)' $t_int_string">$a1->t_int_string = $a2->t_int_A</error>;
    <error descr="Can't assign 'tuple(int, A)' to 'tuple(int, string)' $t_int_string">$a1->t_int_string = getTupleIntA()</error>;
    $a1->t_int_A = $a2->t_int_A;
    $a1->t_int_A = getTupleIntA();
    <error descr="Can't assign 'tuple(int, A)' to 'shape(i:int, a:A)' $s_int_a">$a1->s_int_a = $a2->t_int_A</error>;
    <error descr="Can't assign 'shape(i:int, a:A)' to 'tuple(int, A)' $t_int_A">$a1->t_int_A = $a2->s_int_a</error>;
}

function demo2() {
    $a = new A;
    <error descr="Can't assign 'shape(x:int)' to 'tuple(int, A)' $t_int_A">$a->t_int_A = shape(['x'=>1])</error>;
    <error descr="Can't assign 'tuple(int, A)' to 'shape(i:int, a:A)' $s_int_a">$a->s_int_a = tuple(1, new A)</error>;
}
