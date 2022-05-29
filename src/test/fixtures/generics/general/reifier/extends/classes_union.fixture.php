<?php

namespace Extends\Reifier\ClassUnion;

class Foo { public function fooMethod() {} }
class Boo { public function booMethod() {} }
class FooChild extends Foo { public function fooChildMethod() {} }

interface Stringable { public function __toString(): string; }
class FooStringable implements Stringable {
  public function fooStringableMethod() {}
  public function __toString(): string { return "Hello World"; }
}

/**
 * @kphp-generic T: Foo|Boo
 * @param T $a
 * @return T
 */
function takeFooBoo($a) {
  expr_type($a, "\Extends\Reifier\ClassUnion\Boo|\Extends\Reifier\ClassUnion\Foo");
  return $a;
}

$a = takeFooBoo(new Foo);
$a->fooMethod();
$a->booMethod();
expr_type($a, "\Extends\Reifier\ClassUnion\Foo");

$b = takeFooBoo(new Boo);
$b->booMethod();
$b->fooMethod();
expr_type($b, "\Extends\Reifier\ClassUnion\Boo");

$c = takeFooBoo(new FooChild);
$c->fooChildMethod();
$c->booMethod();
$c->fooMethod();
expr_type($c, "\Extends\Reifier\ClassUnion\FooChild");


/**
 * @kphp-generic T1: Foo|Boo, T2: Stringable|string
 * @param T1 $a
 * @param T2 $b
 * @return T1|T2
 */
function takeFooBooAndString($a, $b) {
  expr_type($a, "\Extends\Reifier\ClassUnion\Boo|\Extends\Reifier\ClassUnion\Foo");
  expr_type($b, "\Extends\Reifier\ClassUnion\Stringable|string");
  return $a;
}

$d = takeFooBooAndString(new Foo, "Hello World");
$d->fooMethod();
expr_type($d, "\Extends\Reifier\ClassUnion\Foo|string");

$e = takeFooBooAndString(new Foo, new FooStringable());
$e->fooMethod();
$e->fooStringableMethod();
expr_type($e, "\Extends\Reifier\ClassUnion\Foo|\Extends\Reifier\ClassUnion\FooStringable");
