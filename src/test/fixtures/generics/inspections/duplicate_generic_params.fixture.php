<?php

/** <error descr="Expected: T[: ExtendsClass] [, T1[: ExtendsClass] [= DefaultType], ...]">@kphp-generic</error><error descr="Expected: Generic argument name (like T)"> </error>*/
function f() {}

/** <error descr="Expected: T[: ExtendsClass] [, T1[: ExtendsClass] [= DefaultType], ...]">@kphp-generic T:</error><error descr="Expected: Extends class name"> </error>*/
function f1() {}

/** @kphp-generic T: Foo */
function f2() {}

/** <error descr="Expected: T[: ExtendsClass] [, T1[: ExtendsClass] [= DefaultType], ...]">@kphp-generic T =</error><error descr="Expected: Default type name"> </error>*/
function f3() {}

/** <error descr="Expected: T[: ExtendsClass] [, T1[: ExtendsClass] [= DefaultType], ...]">@kphp-generic T: Foo =</error><error descr="Expected: Default type name"> </error> */
function f4() {}

/** @kphp-generic T: Foo = Foo */
function f5() {}

/** @kphp-generic T,<error descr="Expected: Generic argument name (like T)"> </error>*/
function f6() {}

/**
 * <error descr="Duplicate generic type T in declaration">@kphp-generic T, T</error>
 */
function takeSomethingOther() {}

/**
 * <error descr="Duplicate generic type T1 in declaration">@kphp-generic T, T1, T1</error>
 */
class SomeClass {
  /**
   * <error descr="Duplicate generic type T (first seen in class declaration)">@kphp-generic T</error>
   */
  function takeSomethingOther() {}
}
