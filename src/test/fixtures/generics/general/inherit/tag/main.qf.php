<?php

namespace Inherit\Tag\Main;

/** @kphp-generic T */
class GenericClass {}
/** @kphp-generic T, T2 */
interface GenericInterface {}

class NonGenericClass {}
interface NonGenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit GenericClass<T>
 */
class Foo1 extends GenericClass {}

/**
 * @kphp-generic T
 * @kphp-inherit GenericInterface<T, T>
 */
class Foo2 implements GenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit GenericInterface<T, T>
 */
class Foo3 implements GenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit GenericInterface<T, T>
 */
class Foo4 implements GenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit GenericClass<T>, GenericInterface<T, T>
 */
class Foo5 extends GenericClass implements GenericInterface {}

/**
 * @kphp-generic T
 */
class Foo6 extends NonGenericClass {}

/**
 * @kphp-generic T
 */
class Foo7 implements NonGenericInterface {}

/**
 * @kphp-generic T
 */
class Foo8 implements NonGenericInterface {}
