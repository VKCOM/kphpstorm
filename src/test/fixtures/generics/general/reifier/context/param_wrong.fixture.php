<?php

namespace Context\Param\Wrong;

use Context\GenericClass;
use Context\IGenericClass;

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

/**
 * @kphp-generic T: IGenericClass
 * @return GenericClass<T>
 */
function genericFunction() { return null; }

TakeWrongGeneric::takeStaticGenericFunction(<error descr="Reified generic type for T is not within its bounds (string is not implement \Context\IGenericClass)">genericFunction()</error>);
