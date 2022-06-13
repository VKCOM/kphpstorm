<?php

namespace Methods\Chain;

use Pair;
use Vector;

class Foo {}
class Boo {}
class Goo {}

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

$a = templateFu(Foo::class);
expr_type($a, "\Methods\Chain\Foo");

$b = returnVector()->get(0);
expr_type($b, "\Vector|\Vector(\Vector|\Vector(string))");

$c = $b->get(0)->get(0);
expr_type($c, "string");


/**
 * @return Pair<Boo, Goo>
 */
function returnPair(): Pair {
  return new Pair(new Boo, new Goo);
}

$x = returnPair()->first();
$y = returnPair()->second();
expr_type($x, "\Methods\Chain\Boo");
expr_type($y, "\Methods\Chain\Goo");


/**
 * @return Vector<Pair<Boo, Goo>>
 */
function returnVectorPair() {
  return new Vector;
}

$d = returnVectorPair()->get(0)->second();
expr_type($d, "\Methods\Chain\Goo");

$e = returnVectorPair()->get(0)->first();
expr_type($e, "\Methods\Chain\Boo");
