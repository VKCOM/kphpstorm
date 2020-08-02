<?php

function demo1(int $s) {}

function demo2($i) {}

function demo3($a, $b, $c) {}

function demo4($a, $b, $c = 1) {}

function demo5(string $s_implicit_null = null) {}

demo1(<error descr="No value passed for $s">)</error>;
demo1(4);
demo1(4, '5');      // ok, it is errored by another inspection
demo1(4);
demo2(<error descr="No value passed for $i">)</error>;
demo3(1, <error descr="No value passed for $b">)</error>;
demo3(1, 2, <error descr="No value passed for $c">)</error>;
demo3(1, 2, 3);
demo4(1, <error descr="No value passed for $b">)</error>;
demo4(1, 2);
demo4(1, 2, 3);
demo4(1, 2, 3, 4);  // ok, it is errored by another inspection
demo5();
demo5(null);
demo5('s');
demo5(<error descr="Can't pass 'array' to '?string' $s_implicit_null">[]</error>);
