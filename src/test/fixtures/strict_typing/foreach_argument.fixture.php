<?php

class A {
    public int $a = 0;
}

class Indexable implements \ArrayAccess {
}

// this is not supported and still foreach() of such class gives an error
class MyForeachable implements \Iterator {
}

/** @return mixed */
function getVar() {}

function getAny($any) { return $any; }

/** @return int[] */
function getIntArr() {}

function getVoid() : void {}

function getString() { return '123'; }

function getAnyArr() : array { return []; }

function getObject() { return new A; }

function getIndexable() { return new Indexable; }

/** @return tuple(int, A) */
function getTuple() { }

/** @return shape(i: int, a: A) */
function getShape() { }


function demo1() {
    foreach([] as $v);
    foreach(getVar() as $v);
    foreach(getVar()['v'] as $v);
    foreach(getIntArr() as $v);
    foreach(<error descr="Invalid foreach on 'int'">getIntArr()[0]</error> as $v);
    foreach(getAnyArr() as $v);
    foreach(getAnyArr()[0] as $v);
    foreach(<error descr="Invalid foreach on 'int'">5</error> as $v);
    foreach(<error descr="Invalid foreach on 'string'">getString()</error> as $v);
    foreach(<error descr="Invalid foreach on 'A'">getObject()</error> as $v);
    foreach(<error descr="Invalid foreach on 'Indexable'">getIndexable()</error> as $v);
    foreach(getAny() as $v);
    foreach(<error descr="Invalid foreach on 'void'">getVoid()</error> as $v);
    foreach(<error descr="Invalid foreach on 'tuple(int, A)'">getTuple()</error> as $v);
    foreach(<error descr="Invalid foreach on 'shape(i:int, a:A)'">getShape()</error> as $v);
    // not supported, even for plain php
    foreach(<error descr="Invalid foreach on 'MyForeachable'">new MyForeachable</error> as $v);

    $a = new A;
    foreach(<error descr="Invalid foreach on 'A'">$a</error> as $v);
    foreach(<error descr="Invalid foreach on 'int'">$a->a</error> as $v);
    foreach($a->unexisting_prop as $v);
}
