<?php

class A {}

function tuple(...$args) {
    return ${'args'};
}

function shape(array $associative_arr) {
    return ${'associative_arr'};
}


/** @return tuple(int, int) */
function getT1() {
    return tuple(1, 2);
}

/** <error descr="Actual return type is 'tuple(int)'">@return</error> tuple(int, string, A) */
function getT2() {
    <error descr="Can't return 'tuple(int)', expected 'tuple(int, string, A)'">return tuple(1);</error>
}

/**
  * <error descr="Actual return type is 'null'">@return</error> tuple(int, int)
  */
function getT3() {
    <error descr="Can't return 'null', expected 'tuple(int, int)'">return null;</error>
}

/** <error descr="Actual return type is 'shape()'">@return</error> tuple(int, int) */
function getT4() {
    <error descr="Can't return 'shape()', expected 'tuple(int, int)'">return shape(['x'=>1]);</error>
}

function getT5() : int {
    <error descr="Can't return 'tuple(int)', expected 'int'">return tuple(5);</error>
}

/** <error descr="Actual return type is 'tuple(int, int)|shape()'">@return</error> tuple(int, int) */
function getT6() {
    // this message is slightly unexpected: it is because getT4() is corrupted by inferred shape
    <error descr="Can't return 'tuple(int, int)|shape()', expected 'tuple(int, int)'">return getT4();</error>
}

/** @return shape(x: string, y: int) */
function getS1() {
    return shape(['x' => '', 'y' => 0]);
}

/** @return shape(x: string, y: int) */
function getS2() {
    // for now, all shapes are compatible with each other
    return shape(['a' => 1]);
}

/** <error descr="Actual return type is 'null'">@return</error> shape(x: string, y: int) */
function getS3() {
    <error descr="Can't return 'null', expected 'shape(x:string, y:int)'">return null;</error>
}

/** <error descr="Actual return type is 'tuple(int, int)'">@return</error> shape(x: string, y: int) */
function getS4() {
    <error descr="Can't return 'tuple(int, int)', expected 'shape(x:string, y:int)'">return tuple(3, 4);</error>
}

/** <error descr="Actual return type is 'object'">@return</error> shape(x: string, y: int) */
function getS5(object $a) {
    <error descr="Can't return 'object', expected 'shape(x:string, y:int)'">return $a;</error>
}




