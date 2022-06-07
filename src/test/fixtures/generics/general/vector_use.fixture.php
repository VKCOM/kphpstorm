<?php

namespace VectorUse;

use Vector;
use Pair;

class Foo {
  function fooMethod(): Boo {
    return new Boo;
  }
}

class Boo {
  function booMethod(): Foo {
    return new Foo;
  }
}

class Goo {
  function getName(): string {
    return "";
  }

  function gooMethod(): Boo {
    return new Boo;
  }
}

/**
 * @kphp-generic T, T2
 * @param T[]            $a
 * @param callable(T):T2 $fn
 * @return T2[]
 */
function map(array $a, callable $fn) {
  return []; // ...
}

$a = map/*<Goo, Boo>*/([new Goo], function(Goo $a): Boo {
  return $a->gooMethod();
});

$b = $a[0];
$b->booMethod();

$vec = new Vector/*<?Goo>*/ ();

$vec->add(new Goo);
$vec->add(null);

$vec->filter(function(?Goo $el): bool {
  return $el != null;
});

$vec->foreach(function(Goo $el) {
  var_dump($el);
});

$vec->foreach(fn(Goo $el) => $el->getName());

$vec->foreach_key_value(function(string $key, Goo $el) {
  var_dump($key);
  var_dump($el);
});

$a = $vec->map/*<?Goo>*/(function(Goo $a): Boo {
  return $a->gooMethod();
});

$b = $a->get(0);

$c = $b->gooMethod()->booMethod()->fooMethod();

/**
 * @return Vector<Vector<Foo>>
 */
function returnVector() {
  return new Vector;
}

$a = returnVector()->get(0);

$a->get(0)->fooMethod();

/**
 * @return Pair<Boo, Goo>
 */
function returnPair(): Pair {
  return new Pair(new Boo, new Goo);
}

$x = returnPair()->second();
$x->gooMethod();

$vecGoo = new Vector/*<Goo>*/();
$vecBoo = new Vector/*<Boo>*/();

$combinedVec = $vecGoo->combine_with($vecBoo);
$combinedVec->get(0)->booMethod();
$combinedVec->get(0)->gooMethod();
