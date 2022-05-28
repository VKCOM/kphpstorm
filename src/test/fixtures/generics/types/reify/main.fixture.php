<?php

namespace Reify;

class Foo {}
class Boo {}

/**
 * @kphp-generic T
 * @param ?T $a
 * @return T
 */
function takeNullable($a) {
    return $a;
}

$a = new Foo();
if (0) {
    $a = null;
}

$b = takeNullable($a);
expr_type($b, "\Reify\Foo");


/**
 * @kphp-generic T
 * @param class-string<T> $a
 * @return T
 */
function takeClassString($a) {
    return new $a;
}

$c = takeClassString(Foo::class);
expr_type($c, "\Reify\Foo");


/**
 * @kphp-generic T
 * @param class-string<T>[] $a
 * @return T
 */
function takeArray($a) {
    return new $a;
}

$c = takeArray([Foo::class]);
expr_type($c, "\Reify\Foo");


/**
 * @kphp-generic T1, T2, T3
 * @param tuple(T1, T2, T3) $a
 * @return T1|T2|T3
 */
function takeTuple($a) {
    return new $a;
}

// TODO: разобраться с таплами
//$c = takeTuple(tuple(1, "", new Foo));
//expr_type($c, "\Reify\Foo");
