<?php

/**
 * @return Vector<Vector<Vector<string>>>
 */
function returnVector() {
  return new Vector;
}

/**
 * @kphp-generic T
 * @param class-string<T> $class
 * @return T
 */
function templateFu($class) {
  return new $class();
}

templateFu(Foo::class)->boo();

$a = returnVector()->get(0);
expr_type($a, "\Vector|\Vector(\Vector|\Vector(string))");

$b = $a->get(0)->get(0);
// TODO: должно быть string, а не null
//expr_type($b, "string");


/**
 * @return Pair<Boo, Goo>
 */
function returnPair(): Pair {
  return new Pair(new Boo, new Goo);
}

$x = returnPair()->first();
$y = returnPair()->second();
expr_type($x, "\Boo");
expr_type($y, "\Goo");

/**
 * @return Vector<Pair<Boo, Goo>>
 */
function returnVectorPair() {
  return new Vector;
}

$a = returnVectorPair()->get(0)->second();
expr_type($a, "\Goo");

