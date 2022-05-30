<?php

namespace Context;

interface IGenericClass {}
interface InterfaceForGenericClass {}

/**
 * @kphp-generic T
 */
class GenericClass implements IGenericClass {}

/**
 * @kphp-generic T1, T2
 */
class GenericClassWithSeveralTypes implements IGenericClass {}

class ClassWithGenericMethod {
  /**
   * @kphp-generic T
   * @return GenericClass<T>
   */
  function genericMethod() { return null; }
}
