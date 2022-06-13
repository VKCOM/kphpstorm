<?php

interface SomeI { function f(); }

class A implements SomeI { function f() { echo "A f\n"; } }
class B implements SomeI { function f() { echo "B f\n"; } }
class C { function f() { echo "C f\n"; } }

/**
 * @kphp-generic T: SomeI
 * @param T $a
 */
function take_some_i($a) {
  if ($a !== null)
    $a->f();
}

take_some_i(new A);
take_some_i(new B);
take_some_i(<error descr="Reified generic type for T is not within its bounds (\C is not implement \SomeI)">new C</error>);
take_some_i/*<A>*/(new A);
take_some_i/*<B>*/(new B);
take_some_i<error descr="Explicit generic type for T is not within its bounds (\C is not implement \SomeI)">/*<C>*/</error>(new C);
take_some_i/*<?B>*/(null);
