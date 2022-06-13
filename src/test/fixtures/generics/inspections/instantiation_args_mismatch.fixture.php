<?php
/** @noinspection PhpExpressionResultUnusedInspection */
/** @noinspection PhpUndefinedConstantInspection */

// Не шаблонный класс
class NotGeneric {}

new NotGeneric(); // ok


// Класс с одним шаблонным типом и без конструктора
/** @kphp-generic T */
class GenericT {}

new GenericT<error descr="1 generic parameters expected, but 0 passed">/*<<error descr="Expected: expression"></error>>*/</error>();
new GenericT/*<int>*/(); // ok


// Класс с двумя шаблоннымм типом
/** @kphp-generic T1, T2 */
class GenericT1T2 {}

new GenericT1T2<error descr="2 generic parameters expected, but 0 passed">/*<<error descr="Expected: expression"></error>>*/</error>();
new GenericT1T2<error descr="2 generic parameters expected, but 1 passed">/*<int>*/</error>();
new GenericT1T2/*<int, string>*/(); // ok


// Класс с одним шаблонным типом с конструктором с еще одним шаблонным типом
/** @kphp-generic T */
class GenericExplicitConstructorT1AndT {
  /**
   * @kphp-generic T1
   * @param T1 $el
   */
  function __construct($el) {}
}

new GenericExplicitConstructorT1AndT/*<int, string>*/("");

// Функция с дефолтными шаблонными параметрами
/**
 * @kphp-generic T1, T2 = Vector<T1>
 * @param T1 $a
 * @return T2
 */
function foo($a) { return $a; }

$a = foo/*<Foo>*/(new Foo);
$a = foo/*<Foo, int>*/(new Foo);
$a = foo<error descr="Not enough generic parameters, expected at least 1">/*<<error descr="Expected: expression"></error>>*/</error>(new Foo);
$a = foo<error descr="Too many generic parameters, expected at most 2">/*<Foo, int, string>*/</error>(new Foo);

/**
 * @kphp-generic T1, T2 = Vector<T1>, T3 = int
 * @param T1 $a
 * @return T2
 */
function foo1($a) { return $a; }

$a = foo1/*<Foo>*/(new Foo);
$a = foo1/*<Foo, int>*/(new Foo);
$a = foo1<error descr="Not enough generic parameters, expected at least 1">/*<<error descr="Expected: expression"></error>>*/</error>(new Foo);
$a = foo1/*<int>*/(100);
$a = foo1/*<Foo, int, string>*/(new Foo);
$a = foo1<error descr="Too many generic parameters, expected at most 3">/*<Foo, int, string, bool>*/</error>(new Foo);


/**
 * @param <error descr="1 generic parameters expected, but 0 passed">GenericT</error> $a
 * @param GenericT<int> $a1
 * @param <error descr="1 generic parameters expected, but 2 passed">GenericT<int, string></error> $a2
 */
function foo2($a, $a1, $a2) {}


/**
 * @kphp-generic T1 = GenericT, T2 = GenericT<int>
 */
function foo3() {}


/**
 * @kphp-generic T1 = <error descr="1 generic parameters expected, but 2 passed">GenericT<int, string></error>
 */
function foo4() {}
