<?php

function getInt1() : int {
    <error descr="Can't return 'string', expected 'int'">return 'asdf';</error>
}

function getInt2() : int {
    return 1;
}

function getInt3() : int {
    return getInt1();
}

/** @return int */
function getInt4() {
    $i = 1 ? 4 : 10;
    return $i;
}

/** <error descr="Actual return type is '?int'">@return</error> int */
function getInt5() {
    $i = 1 ? null : 4;
    <error descr="Can't return '?int', expected 'int'">return $i;</error>
}

/** @return int */
function getInt6() : int {
    $i = 1 ? null : 4;
    if($i === null)
        return 0;
    return $i;
}

function getInt7() : void {
    <error descr="Can't return 'int', expected 'void'">return <error descr="A void function must not return a value">5</error>;</error>
}

function getInt8() : int {
    <error descr="Can't return 'void', expected 'int'">return;</error>
}

/** <error descr="Actual return type is 'null'">@return</error> int */
function getInt9() {
    <error descr="Can't return 'null', expected 'int'">return null;</error>
}

function getInt10() : int {
    // no error, there is another inspection, "Inconsistent return points"
}

function getInt11() : int {
    if(1) return 1;
    // no error, there is another inspection, "Inconsistent return points"
}

function getVoid1() : void {
}

function getVoid2() : void {
    return;
}

function getVoid3() : void {
    if(1) return;
}


/**
 * @param string $str
 * @param string[] $attr
 * @return string[]
 */
function parseString($str, $attr = []) {
    if ($str && preg_match_all('/(?<name>[0-9a-z\-]+)=(\'|")(?<value>.*?)\\2/', $str, $matches, PREG_SET_ORDER)) {
        return array_reduce($matches, function($attr, $match) {
            $attr[$match['name']] = (string)$match['value'];
            return $attr;
        }, $attr);
    }
    return $attr;
}
