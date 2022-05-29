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
