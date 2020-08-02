<?php

class A {
    /** @var object */
    public $o;
    /** @var ?object */
    public $o_null;
}

class B {}

function getO() : object {
}

function demo(object $o) {
    $a = new A;
    $a->o = new A;
    $a->o = new B;
    $a->o = $o;
    $a->o = getO();
    $a->o = null;
    <error descr="Can't assign 'object[]' to 'object' $o">$a->o = [$o]</error>;
    $a->o_null = new A;
    $a->o_null = new B;
    $a->o_null = $o;
    $a->o_null = getO();
    $a->o_null = null;
    <error descr="Can't assign 'object[]' to '?object' $o_null">$a->o_null = [$o]</error>;
}


