<?php

interface I {}

class A1 implements I {}
class A2 implements I {}

class BaseObject {}
class Suggest extends BaseObject {}

class Holder {
    /** @var I */
    public $i;
    /** @var A1 */
    public $a1;

    /** @var BaseObject[] */
    public $objects = [];
    /** @var Suggest[] */
    public $suggests = [];
}

function demo1() {
    switch(rand()) {
        case 1:
            $i = new A1;
            break;
        default:
            $i = null;
    }

    $holder = new Holder;
    $holder->i = $i;
    $holder->a1 = $i;
}

function demo2() {
    switch(rand()) {
        case 1:
            $i = new A1;
            break;
        default:
            $i = new A2;
    }

    $holder = new Holder;
    $holder->i = $i;
    <error descr="Can't assign 'A1|A2' to 'A1' $a1">$holder->a1 = $i</error>;
}

function demo3(BaseObject $o) {
    $h = new Holder;
    $h->objects[] = $o;
    if ($o instanceof Suggest) {
        $h->objects[] = $o;
        $h->suggests[] = $o;
    }
    $h->objects[] = instance_cast($o, Suggest::class);
    $h->suggests[] = instance_cast($o, Suggest::class);
}
