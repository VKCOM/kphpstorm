<?php

interface SomeI { function f(); }

class A implements SomeI { function f() { echo "A f\n"; } }
class B implements SomeI { function f() { echo "B f\n"; } }
class C { function f() { echo "C f\n"; } }

/**
 * @kphp-generic T: SomeI
 * @param T $o
 */
function take_some_i($o) {
  if ($o !== null)
    $o->f();
}

take_some_i(new A);
take_some_i(new B);
take_some_i(<error descr="Reified generic type for T is not within its bounds (\C not implements \SomeI)">new C</error>);
take_some_i/*<A>*/(new A);
take_some_i/*<B>*/(new B);
take_some_i<error descr="Explicit generic type for T is not within its bounds (\C not implements \SomeI)">/*<C>*/</error>(new C);
take_some_i/*<?B>*/(null);
