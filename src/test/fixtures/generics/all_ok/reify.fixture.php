<?php

namespace AllOk\Reify;

class Foo {
    function fooMethod() {}
}
class Boo {
    function booMethod() {}
}
class Goo {
    function gooMethod() {}
}

/**
 * @kphp-generic T
 * @param T $first
 * @param T ...$rest
 * @return T
 */
function f8($first, ...$rest) { return $first; }

f8(new Foo, new Foo, new Foo)->fooMethod();
f8(new Boo, new Boo)->booMethod();
f8(new Goo)->gooMethod();


class F10 {
    /**
     * @kphp-generic T
     * @param T ...$all
     * @return T
     */
    function f11(...$all) { return $all[0]; }
}

$f10 = new F10;
(function() use($f10) {
    $f10->f11(new Foo, new Foo, new Foo)->fooMethod();
    $f10->f11(new Boo)->booMethod();
    $f10->f11/*<Goo>*/()->gooMethod();
})();


/**
 * @kphp-generic T
 * @param callable(T,T): bool $gt
 * @param T ...$arr
 * @return T
 */
function maxBy($gt, ...$arr) {
    $max = array_first_value($arr);
    for ($i = 1; $i < count($arr); ++$i) {
        if ($gt($arr[$i], $max))
            $max = $arr[$i];
    }
    return $max;
}

echo maxBy/*<int>*/(fn ($a, $b) => $a > $b, 1, 2, 9, 3), "\n";
echo maxBy/*<string>*/(fn ($a, $b) => ord($a) > ord($b), 'a', 'z', 'd'), "\n";
