<?php

class A {
    /**
     * @param int $a
     * @param ?A $b
     */
    function f1($a, $b, string $c) {}
}

class B1 extends A {
}

class B2 extends A {
    function f1($a, $b, $c) {
        parent::f1($a, $b, $c);
    }
}

class B22 extends B2 {
    function f1($a, $b, $c) {
        parent::f1($a, $b, $c);
    }
}

function demo() {
    $a = new A;
    $b1 = new B1;
    $b2 = new B2;
    $b22 = new B22;

    $a->f1(<error descr="Can't pass 'string' to 'int' $a">'s'</error>, <error descr="Can't pass 'int' to '?A' $b">3</error>, <error descr="Can't pass 'int' to 'string' $c">4</error>);
    $b1->f1(<error descr="Can't pass 'string' to 'int' $a">'s'</error>, <error descr="Can't pass 'int' to '?A' $b">3</error>, <error descr="Can't pass 'int' to 'string' $c">4</error>);
    $b2->f1(<error descr="Can't pass 'string' to 'int' $a">'s'</error>, <error descr="Can't pass 'int' to '?A' $b">3</error>, <error descr="Can't pass 'int' to 'string' $c">4</error>);
    $b22->f1(<error descr="Can't pass 'string' to 'int' $a">'s'</error>, <error descr="Can't pass 'int' to '?A' $b">3</error>, <error descr="Can't pass 'int' to 'string' $c">4</error>);
}
