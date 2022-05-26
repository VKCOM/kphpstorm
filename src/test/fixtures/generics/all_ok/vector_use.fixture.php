<?php

class Boo {
  function foo(): Foo {
    return new Foo;
  }
}

class Goo {
  function boo(): Boo {
    return new Boo;
  }
}

///**
// * @kphp-generic T, T2
// * @param T[]            $a
// * @param callable(T):T2 $fn
// * @return T2[]
// */
//function map(array $a, callable $fn) {
//  return []; // ...
//}
//
//$a = map/*<Goo, Boo>*/([new Goo], function(Goo $a): Boo {
//  return $a->boo();
//});
//
//$b = $a[0];
//$b->foo();

$vec = new Vector/*<?Goo>*/ ();

//$vec->add(new Goo);
//$vec->add(null);
//
//$vec->filter(function(?Goo $el): bool {
//  return $el != null;
//});

//$a = $vec->map/*<?Goo>*/(function(Goo $a): Boo {
//  return $a->boo();
//});

//
//$b = $a->get(0);
//
//$c = $b->boo()->foo()->boo();

/**
 * @return Vector<Vector<string>>
 */
function returnVector() {
  return new Vector;
}


$a = returnVector()->get(0);

$b = $a->get(0)->get(0);

/**
 * @return Pair<Boo, Goo>
 */
function returnPair(): Pair {
  return new Pair(new Boo, new Goo);
}

$x = returnPair()->second();




//$vec->foreach(function(Goo $el) {
//  var_dump($el);
//});
//
//$vec->foreach(fn(Goo $el) => var_dump($el));
//
//$vec->foreach_key_value(function(string $key, Goo $el) {
//  var_dump($key);
//  var_dump($el);
//});
