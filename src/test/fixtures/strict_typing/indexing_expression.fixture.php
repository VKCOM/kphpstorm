<?php

class A {
    public int $a = 0;
}

// not supported, and still indexing such objects is highlighted as error
class Indexable implements \ArrayAccess {
}

/** @return int|string|null */
function array_first_key(array &$a) {
  reset($a);
  return key($a);
}

function tuple(...$args) {
    return ${'args'};
}

function shape(array $associative_arr) {
    return ${'associative_arr'};
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

/** @return tuple(int, A)|false */
function getTupleOrFalse() { return false; }

/** @return shape(i: int, a: A) */
function getShape() { }

/** @return int|string */
function getIntOrString() {}


function demo1() {
    getAnyArr()[1];
    getAnyArr()[1][2][3+4];
    getVar()[1];
    getVar()[1][2][3];
    getIntArr()[1];
    <error descr="Invalid indexing of 'int'">getIntArr()[1][2]</error>;
    <error descr="Invalid indexing of 'int'">getIntArr()[1][2]</error>[3];
    <error descr="Invalid indexing of 'null'">getVoid()[1]</error>;
    getTuple()[0];
    getTuple()['ddd'];
    getTuple()[$unknown_idx];
    getShape()[0];
    getShape()['ddd'];
    <error descr="Invalid indexing of 'int'">getTuple()[0][1]</error>;
    <error descr="Invalid indexing of 'A'">getObject()[0]</error>;
    <error descr="Invalid indexing of 'int'">getObject()->a[0]</error>;
    getObject()->unknown_idx[0];
    getString()[1];
    getString()[1][3];
    getIntOrString()[3];
    getIntOrString()[3][3];
    // not supported: ArrayAccess
    <error descr="Invalid indexing of 'Indexable'">getIndexable()[0]</error>;

    $t = tuple(1, 'd');
    $t[0];

    /** @var $sss string */
    $sss[4];

    /** @var $iii int */
    <error descr="Invalid indexing of 'int'">$iii[0]</error>;
}

function demo2() {
    getIntArr()[<error descr="Invalid index of type 'int[]'">getIntArr()</error>];
    []['d'.getIntArr()];
    [][<error descr="Invalid index of type 'tuple(int, A)'">getTuple()</error>];
    'd'[<error descr="Invalid index of type 'Indexable'">getIndexable()</error>];
    [][<error descr="Invalid index of type 'A'">getObject()</error>];
    [][getVar()];
    [][<error descr="Invalid index of type 'array'">getAnyArr()</error>];
    [][3.4];
    [][-34e3];
    [][getAny()];
    [][getAny()[getAny()]];
    [][getObject()->a];
    [][getIntOrString()];
    [][array_first_key([])];

    $group_ids = 1 ? false : ['str'];
    foreach($group_ids as $group_id)
        [][$group_id];

    // still available, as PhpStorm treats false/bool often incorrectly
    $int_or_false = 1 ? false : 5;
    [][$int_or_false];

    $arr2 = [];
    while( ($line = fgets('')) !== false) {
        $arr2[$line] = $line;
    }
}

function demo3() {
    global $unknown_global;
    $i = 3;
    <error descr="Invalid indexing of 'int'">$i[<error descr="Invalid index of type 'A'">getObject()</error>]</error>;
    $unknown[$unknown];
    [][$unknown_global];
    [$unknown_global][$unknown_global];
}

function demo4() {
    $arr = getAny();
    foreach($arr as $k => $_) {
        $arr[$k];
        getIntArr()[$k];
    }
}

function demo5($user = false) {
    $id = $user['id'];
}

function demo6() {
  $ads_sections_mobile = [1,2,3];
  $ads_section_mobile   = $ads_sections_mobile[0];

  if (rand() > 1 || is_array($ads_section_mobile)) {
  }

  [][<error descr="Invalid index of type 'int[]'">$ads_sections_mobile</error>];
  [][$ads_section_mobile];
}

function getNullableAny() {
    $m = [];
    if(rand()) return $m[0];
    return null;
}

function demo7() {
    [][getNullableAny()];
    [getNullableAny()][0];
}

function demo8() {
    $t = getTupleOrFalse();
//     if(!$t) return;
    $t[0];
    $t[1];
}
