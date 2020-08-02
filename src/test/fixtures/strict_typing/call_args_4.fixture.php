<?php

class A {}

function f1(int $a, int $b, int $c) {}
function f2() {}

function f3(int ...$args) {}
function f4(int $a, string ...$args) {}

class F {
    /**
     * @param int $a
     * @param string[] $args
     */
    function f5($a, ...$args) {}
    /**
     * @param int $a
     * @param string ...$args
     */
    function f6($a, ...$args) {}
}

f1(...[1,2,3]);
f1(...[]);
f1(...[new A]);
f2(...[1,2,3]);

f3();
f3(1, 2, 3);
f3(<error descr="Can't pass 'string' to 'int' $args">'1'</error>, <error descr="Can't pass 'string' to 'int' $args">'2'</error>, <error descr="Can't pass 'string' to 'int' $args">'3'</error>);
f3(1, <error descr="Can't pass 'string' to 'int' $args">'2'</error>, <error descr="Can't pass 'string' to 'int' $args">'3'</error>);
f3(1, 2, <error descr="Can't pass 'A' to 'int' $args">new A</error>);
f3(1, 2, ...[new A]);
f3(1, 2, ...[new A], <error descr="Cannot use positional argument after argument unpacking">4</error>);
f3(1, 2, ...[new A], ...[new A]);

f4(<error descr="No value passed for $a">)</error>;
f4(1);
f4(<error descr="Can't pass 'array' to 'int' $a">[]</error>);
f4(1, '2');
f4(1, '2', '3');
f4(1, '2', '3', <error descr="Can't pass 'int' to 'string' $args">4</error>, <error descr="Can't pass 'A' to 'string' $args">new A</error>);
f4(1, '2', '3', ...[new A, 4], ...['6']);
f4(1, '2', '3', ...[new A, 4]);

$f = new F;

$f->f5(<error descr="No value passed for $a">)</error>;
$f->f5(1);
$f->f5(<error descr="Can't pass 'array' to 'int' $a">[]</error>);
$f->f5(1, '2');
$f->f5(1, '2', '3');
$f->f5(1, '2', '3', <error descr="Can't pass 'int' to 'string' $args">4</error>, <error descr="Can't pass 'A' to 'string' $args">new A</error>);
$f->f5(1, '2', '3', ...[new A, 4], ...['6']);
$f->f5(1, '2', '3', ...[new A, 4]);

$f->f6(<error descr="No value passed for $a">)</error>;
$f->f6(1);
$f->f6(<error descr="Can't pass 'array' to 'int' $a">[]</error>);
$f->f6(1, '2');
$f->f6(1, '2', '3');
$f->f6(1, '2', '3', <error descr="Can't pass 'int' to 'string' $args">4</error>, <error descr="Can't pass 'A' to 'string' $args">new A</error>);
$f->f6(1, '2', '3', ...[new A, 4], ...['6']);
$f->f6(1, '2', '3', ...[new A, 4]);
