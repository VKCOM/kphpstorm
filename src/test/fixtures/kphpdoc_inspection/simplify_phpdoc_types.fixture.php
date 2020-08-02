<?php
namespace asdf;

class A {
    /** @var \<weak_warning descr="Leading slash is not necessary">tuple(int, A)</weak_warning> comment */
    public $t;
    /** @var \<weak_warning descr="Leading slash is not necessary">shape(x: int, y: A)</weak_warning> comment */
    public $sh;
    /** @var <weak_warning descr="Use '?T', not 'T|null'">int|null</weak_warning> */
    public $i_null;
    /** @var <weak_warning descr="Use '?T', not 'T|null'">tuple(int, string)|null</weak_warning> */
    public $t_null;
    /** @var <weak_warning descr="Use '?T', not 'T|null'">null|int[]</weak_warning> */
    public $int_arr_null;
}

/**
 * @param \<weak_warning descr="Leading slash is not necessary">tuple<int, <weak_warning descr="Use '?T', not 'T|null'">A|null</weak_warning>></weak_warning> $a some
 * @return shape(some: int|null|string, y: <weak_warning descr="Use '?T', not 'T|null'">null|\ns\A[]</weak_warning>, z:?A)
 */
function demo1($a) {
    return shape([]);
}


class inner {}

/** @return <weak_warning descr="Use '?T', not 'T|null'">\asdf\inner|null</weak_warning> */
function demo2(): ?object { return null; }
