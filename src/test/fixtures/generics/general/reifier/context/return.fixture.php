<?php

namespace Context\Return;

use Context\ClassWithGenericMethod;
use Context\GenericClass;
use Context\GenericClassWithSeveralTypes;
use Context\IGenericClass;

/**
 * @return GenericClass<string>
 */
function returnClassOfString() {
  return new GenericClass();
}

$a = returnClassOfString();
expr_type($a, "\Context\GenericClass|\Context\GenericClass(string)");

/**
 * @return GenericClassWithSeveralTypes<string, int>
 */
function returnClassWithSeveralTypesOfStringInt() {
  return new GenericClassWithSeveralTypes();
}


/**
 * @kphp-generic T: IGenericClass
 * @return GenericClass<T>
 */
function genericFunction() { return null; }

/**
 * @return GenericClass<GenericClass<string>>
 */
function returnClassOfStringViaFunction() {
  return genericFunction();
}

/**
 * @return GenericClass<string>
 */
function returnClassOfStringViaGenericMethod() {
  $a = new ClassWithGenericMethod();
  return $a->genericMethod();
}
