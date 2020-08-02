<?php

class A {
    /** @var mixed */
    public $m;

    public string $s;
}

function demo() {
    $a = new A;

    // var[*] is var
    <error descr="Can't assign 'var' to 'string' $s">$a->s = $a->m[0]</error>;
    <error descr="Can't assign 'var' to 'string' $s">$a->s = $a->m[0][0]</error>;
}
