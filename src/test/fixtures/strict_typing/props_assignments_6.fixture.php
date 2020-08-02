<?php

class A {
    /** @var callable */
    public $callable;
    /** @var callable|null */
    public $callable_null = null;
}

/** @return callable[] */
function getCallableArr() {}

/** @return mixed */
function getMixed() {}

function demo1() {
    $a = new A;
    $a->callable = function() {};
    $a->callable_null = function() {};
    <error descr="Can't assign 'null' to 'callable' $callable">$a->callable = null</error>;
    $a->callable_null = null;
    $a->callable_null = $a->callable;
    <error descr="Can't assign '?callable' to 'callable' $callable">$a->callable = $a->callable_null</error>;
    $a->callable = getCallableArr()[0];

    $a->callable = ['A', 'method'];
    $a->callable = 'function';
    <error descr="Can't assign 'int' to 'callable' $callable">$a->callable = 3</error>;
    <error descr="Can't assign 'var' to 'callable' $callable">$a->callable = getMixed()</error>;
}
