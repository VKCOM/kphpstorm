<?php

/**
 * @param GenericClass $a
 */
function takeClassOfString($a) {}

takeClassOfString(<error descr="Not enough information to infer generic T">new GenericClass()</error>);

class TakeWrongGeneric {
  /**
   * @param GenericClass<string> $a
   */
  static function takeStaticGenericFunction($a) {}
}

TakeWrongGeneric::takeStaticGenericFunction(<error descr="Reified generic type for T is not within its bounds (string not implements \IGenericClass)">genericFunction()</error>);

