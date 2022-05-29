<?php

namespace Extends\Wrong;

class Foo {}

/** <error descr="Union type can contain either only instances or only primitives (except '\Stringable|string')">@kphp-generic T: int|Foo</error> */
function take1() {}

/** <error descr="Type '?\Extends\Wrong\Foo' is not allowed here">@kphp-generic T: ?Foo</error> */
function take2() {}

/** <error descr="Type 'force(int)' is not allowed here">@kphp-generic T: force(int)</error> */
function take3() {}

/** <error descr="Type 'tuple(int,\Extends\Wrong\Foo)' is not allowed here">@kphp-generic T: tuple(int, Foo)</error> */
function take4() {}

/** <error descr="Type 'shape(key1:int,key2:\Extends\Wrong\Foo)' is not allowed here">@kphp-generic T: shape(key1: int, key2: Foo)</error> */
function take5() {}

/** <error descr="Type 'class-string(\Extends\Wrong\Foo)' is not allowed here">@kphp-generic T: class-string<Foo></error> */
function take6() {}

/** <error descr="Type '\Extends\Wrong\Foo[]' is not allowed here">@kphp-generic T: Foo[]</error> */
function take7() {}

/** <error descr="Type 'any' is not allowed here">@kphp-generic T: any</error> */
function take8() {}
