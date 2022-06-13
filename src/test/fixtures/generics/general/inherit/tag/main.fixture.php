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

class <error descr="Class extends or implements generic class/interface, please specify @kphp-inherit">Foo3</error> implements GenericInterface {}

/**
 * @kphp-generic T
 */
class <error descr="Class extends or implements generic class/interface, please specify @kphp-inherit">Foo4</error> implements GenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit GenericInterface<T, T>
 */
class Foo5 extends <error descr="Class extends generic class/interface, but this class not specified in @kphp-inherit">GenericClass</error> implements GenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit <error descr="Class/interface \Inherit\Tag\Main\GenericClass not extended or implemented class/interface Foo6">GenericClass<T></error>
 */
class Foo6 extends NonGenericClass {}

/**
 * @kphp-generic T
 * @kphp-inherit <error descr="Class/interface \Inherit\Tag\Main\GenericInterface not extended or implemented class/interface Foo7">GenericInterface<T, T></error>
 */
class Foo7 implements NonGenericInterface {}

/**
 * @kphp-generic T
 * @kphp-inherit <error descr="It is not necessary to specify not generic class/interface \Inherit\Tag\Main\NonGenericInterface in @kphp-inherit">NonGenericInterface</error>
 */
class Foo8 implements NonGenericInterface {}
