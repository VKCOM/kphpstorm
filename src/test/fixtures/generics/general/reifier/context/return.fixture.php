<?php

interface IGenericClass {}

/**
 * @kphp-generic T
 */
class GenericClass implements IGenericClass {}

/**
 * @return GenericClass<string>
 */
function returnClassOfString() {
  return new GenericClass();
}

$a = returnClassOfString();
expr_type($a, "\GenericClass|\GenericClass(string)");

/**
 * @kphp-generic T1, T2
 */
class GenericWithSeveralTypesClass implements IGenericClass {}

/**
 * @return GenericWithSeveralTypesClass<string, int>
 */
function returnClassWithSeveralTypesOfStringInt() {
  return new GenericWithSeveralTypesClass();
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


class ClassWithGenericMethod {
  /**
   * @kphp-generic T
   * @return GenericClass<T>
   */
  function genericMethod() { return null; }
}

/**
 * @return GenericClass<string>
 */
function returnClassOfStringViaGenericMethod() {
  $a = new ClassWithGenericMethod();
  return $a->genericMethod();
}
