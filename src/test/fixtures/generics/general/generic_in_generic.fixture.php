<?php

namespace GenericInGeneric;

class Boo {}

/**
 * @kphp-generic T
 */
class Foo {
  public function method() {
    genericFunction/*<T>*/();
    genericFunction/*<<error descr="Undefined class 'T1'">T1</error>>*/();
  }

  /**
   * @kphp-generic T1
   */
  public function method2() {
    genericFunction/*<T>*/();
    genericFunction/*<T1>*/();
    genericFunction/*<<error descr="Undefined class 'T2'">T2</error>>*/();
  }
}

/**
 * @kphp-generic T
 * @return T
 */
function genericFunction() {
  genericFunction/*<T>*/();
  genericFunction/*<<error descr="Undefined class 'T1'">T1</error>>*/();
}

/**
 * @kphp-generic TKey: int|string, TValue
 */
class Collection {
  /**
   * @return Collection<TValue, TKey>
   */
  public function flip() {
    return new Collection/*<TValue, TKey>*/();
  }
}

$col = new Collection/*<string, int>*/();
$col2 = $col->flip();
expr_type($col2, "\GenericInGeneric\Collection|\GenericInGeneric\Collection(int,string)");
