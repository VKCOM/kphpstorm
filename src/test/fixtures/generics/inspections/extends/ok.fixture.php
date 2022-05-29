<?php

namespace Extends;

use Stringable;

class Foo {}
class Boo {}
class Goo {}

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

// Special allowed instance + primitive case
/** @kphp-generic T: Stringable|string */
function take9() {}
/** @kphp-generic T: \Stringable|string */
function take10() {}
/** @kphp-generic T: string|Stringable */
function take11() {}
/** @kphp-generic T: string|\Stringable */
function take12() {}
