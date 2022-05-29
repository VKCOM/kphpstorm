<?php

namespace Extends\Reifier\Wrong;

interface Stringable {}
interface SomeI {}

class A implements SomeI {}
class B {}
class C {}
class DStringable implements Stringable {}

/**
 * @kphp-generic T: SomeI
 * @param T $a
 */
function take_instance($a) {}

take_instance(new A);
take_instance(<error descr="Reified generic type for T is not within its bounds (\Extends\Reifier\Wrong\B is not implement \Extends\Reifier\Wrong\SomeI)">new B</error>);
take_instance/*<A>*/(new A);
take_instance<error descr="Explicit generic type for T is not within its bounds (\Extends\Reifier\Wrong\B is not implement \Extends\Reifier\Wrong\SomeI)">/*<B>*/</error>(new B);


/**
 * @kphp-generic T: string
 * @param T $a
 */
function take_string($a) {}

take_string("");
take_string(<error descr="Reified generic type for T is not within its bounds (int is not string))">10</error>);
take_string/*<string>*/("");
take_string<error descr="Explicit generic type for T is not within its bounds (int is not string))">/*<int>*/</error>(10);


/**
 * @kphp-generic T: SomeI | B
 * @param T $a
 */
function take_instance_or_other($a) {}

take_instance_or_other(new A);
take_instance_or_other(new B);
take_instance_or_other(<error descr="Reified generic type for T is not within its bounds (\Extends\Reifier\Wrong\C is none extend/implement any of \Extends\Reifier\Wrong\B or \Extends\Reifier\Wrong\SomeI)">new C</error>);
take_instance_or_other/*<A>*/(new A);
take_instance_or_other/*<B>*/(new B);
take_instance_or_other<error descr="Explicit generic type for T is not within its bounds (\Extends\Reifier\Wrong\C is none extend/implement any of \Extends\Reifier\Wrong\B or \Extends\Reifier\Wrong\SomeI)">/*<C>*/</error>(new C);


/**
 * @kphp-generic T: string | int
 * @param T $a
 */
function take_string_or_int($a) {}

take_string_or_int("");
take_string_or_int(100);
take_string_or_int(<error descr="Reified generic type for T is not within its bounds (bool is neither string nor int)">true</error>);
take_string_or_int/*<string>*/("");
take_string_or_int/*<int>*/(100);
take_string_or_int<error descr="Explicit generic type for T is not within its bounds (bool is neither string nor int)">/*<bool>*/</error>(true);


/**
 * @kphp-generic T: string | Stringable
 * @param T $a
 */
function take_stringable_or_string($a) {}

take_stringable_or_string("");
take_stringable_or_string(new DStringable);
take_stringable_or_string(<error descr="Reified generic type for T is not within its bounds (\Extends\Reifier\Wrong\C is not implement a \Stringable and not a string)">new C</error>);
take_stringable_or_string/*<string>*/("");
take_stringable_or_string/*<DStringable>*/(new DStringable);
take_stringable_or_string<error descr="Explicit generic type for T is not within its bounds (\Extends\Reifier\Wrong\C is not implement a \Stringable and not a string)">/*<C>*/</error>(new C);
