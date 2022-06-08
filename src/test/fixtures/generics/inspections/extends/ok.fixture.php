<?php

namespace Extends;

use Stringable;

class Foo {}
class Boo {}
class Goo {}
/** @kphp-generic T */
class Generic {}

/** @kphp-generic T: int */
function take1() {}

/** @kphp-generic T: Foo */
function take2() {}

/** @kphp-generic T: callable */
function take3() {}

/** @kphp-generic T: callable(int, Foo): bool */
function take4() {}

/** @kphp-generic T: int|string */
function take5() {}

/** @kphp-generic T: int|string|bool */
function take6() {}

/** @kphp-generic T: Foo|Boo */
function take7() {}

/** @kphp-generic T: Foo|Boo|Goo */
function take8() {}

/** @kphp-generic T1, T2: T1|Foo */
function take9() {}

/** @kphp-generic T1, T2: Foo|Generic<T1> */
function take10() {}

// Special allowed instance + primitive case
/** @kphp-generic T: Stringable|string */
function take11() {}
/** @kphp-generic T: \Stringable|string */
function take12() {}
/** @kphp-generic T: string|Stringable */
function take13() {}
/** @kphp-generic T: string|\Stringable */
function take14() {}
