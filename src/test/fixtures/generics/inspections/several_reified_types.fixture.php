<?php

/**
 * @kphp-generic T1
 * @param T1 $a
 * @param T1 $b
 * @return void
 */
function takeSomething($a, $b) {}

<error descr="Couldn't reify generic <T1> for call: it's both string and int">takeSomething</error>(100, "");
<error descr="Couldn't reify generic <T1> for call: it's both string and \Foo">takeSomething</error>(new Foo, "");
takeSomething/*<int>*/ (100, "");
