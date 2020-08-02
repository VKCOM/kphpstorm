<?php
namespace asdf;

class A {
    /** @var tuple(int, A) comment */
    public $t;
    /** @var shape(x: int, y: A) comment */
    public $sh;
    /** @var ?int */
    public $i_null;
    /** @var ?tuple(int, string) */
    public $t_null;
    /** @var ?int[] */
    public $int_arr_null;
}

/**
 * @param tuple<int, ?A> $a some
 * @return shape(some: int|null|string, y: ?\ns\A[], z:?A)
 */
function demo1($a) {
    return shape([]);
}


class inner {}

/** @return ?inner */
function demo2(): ?object { return null; }
