<?php

namespace Context\Param;

use Context\ClassWithGenericMethod;
use Context\GenericClass;
use Context\GenericClassWithSeveralTypes;
use Context\IGenericClass;

/**
 * @kphp-generic T: IGenericClass
 * @return GenericClass<T>
 */
function genericFunction() { return null; }

/**
 * @param GenericClass<string> $a
 */
function takeClassOfString($a) {}

takeClassOfString(new GenericClass());


/**
 * @param GenericClassWithSeveralTypes<string, int> $a
 */
function takeClassWithSeveralTypesOfStringInt($a) {}

takeClassWithSeveralTypesOfStringInt(new GenericClassWithSeveralTypes);


/**
 * @param GenericClass<GenericClass<string>> $a
 */
function takeClassOfStringViaFunction($a) {}

takeClassOfStringViaFunction(genericFunction());


/**
 * @param GenericClass<string> $a
 */
function takeClassOfStringViaGenericMethod($a) {}

$a = new ClassWithGenericMethod();
takeClassOfStringViaGenericMethod($a->genericMethod());


class TakeGeneric {
  /**
   * @param GenericClass<string> $a
   */
  function takeGenericFunction($a) {}

  /**
   * @param GenericClass<IGenericClass> $a
   */
  static function takeStaticGenericFunction($a) {}
}

$g = new TakeGeneric;
$g->takeGenericFunction($a->genericMethod());
$g->takeGenericFunction(new GenericClass());
TakeGeneric::takeStaticGenericFunction(genericFunction());
TakeGeneric::takeStaticGenericFunction(new GenericClass());
