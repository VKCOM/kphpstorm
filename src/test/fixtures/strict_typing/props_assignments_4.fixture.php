<?php

class A {
    /** @var int|string */
    public $int_or_string;
    /** @var int|false */
    public $int_or_false;
    /** @var int|string|false */
    public $int_or_string_or_false;
    /** @var int|string|null */
    public $int_or_string_or_null;

    /** @var int[] */
    public $int_arr;
    /** @var (mixed|false|null)[] */
    static $raw_posts_cache = [];

    /** @var A[]|null */
    public $a_arr = null;

    /** @var any */
    public $any;
}

/**
 * @param $platform int|string|false
 */
function setPlatform1($platform) {
    $a = new A;
    $a->int_or_string = $platform;
    <error descr="Can't assign 'int|string|false' to 'int|false' $int_or_false">$a->int_or_false = $platform</error>;
    $a->int_or_string_or_false = $platform;
    $a->int_or_string_or_null = $platform;
}

/**
 * @param $platform int|any
 */
function setPlatform2($platform) {
    $a = new A;
    $a->int_or_string = $platform;
    $a->int_or_false = $platform;
    $a->int_or_string_or_false = $platform;
    $a->int_or_string_or_null = $platform;
}

/**
 * @param $platform string|null
 */
function setPlatform3($platform) {
    $a = new A;
    <error descr="Can't assign '?string' to 'int|string' $int_or_string">$a->int_or_string = $platform</error>;
    <error descr="Can't assign '?string' to 'int|false' $int_or_false">$a->int_or_false = $platform</error>;
    <error descr="Can't assign '?string' to 'int|string|false' $int_or_string_or_false">$a->int_or_string_or_false = $platform</error>;
    $a->int_or_string_or_null = $platform;
}

/**
 * @param int[]|any[] $arg
 */
function setAny1($arg) {
    $a = new A;
    $a->int_arr = $arg;
    $a->int_arr[0] = $arg[0];
}

/**
 * @param int[]|any $arg
 */
function setAny2($arg) {
    $a = new A;
    $a->int_arr = $arg;
    $a->int_arr[0] = $arg[0];
}

/**
 * @param int[]|any|string[] $arg
 */
function setAny3($arg) {
    $a = new A;
    <error descr="Can't assign 'string[]|int[]|any' to 'int[]' $int_arr">$a->int_arr = $arg</error>;
    <error descr="Can't assign 'int|string|any' to 'int' $int_arr[*]">$a->int_arr[0] = $arg[0]</error>;
}

function setPosts() {
    $raw_posts = [];
    $raw_posts[] = A::$raw_posts_cache[1];
    $raw_posts[] = (array)[1,2,3];
    $raw_post = (rand() ? false : null);
    $raw_posts[] = $raw_post;

    A::$raw_posts_cache[] = $raw_posts[0];
}

function setAArr() {
    $a = new A;
    $a->a_arr[] = $a;
}

function setAny() {
    $a = new A;
    $a->any = 5;
    $a->any = [1,2,3];
    $a->any[] = 5;
    $a->any[] = [1,2,3];
}
