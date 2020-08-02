<?php

// don't report incompatibility for standard and kphp functions, because
// 1) kphp casts arguments, so passing float instead of string is ok
// 2) PhpStorm stubs for PHP standard functions sometimes have wrong arguments

// this can't be checked by tests, as PHP standard lib is un available here, use real IDE
in_array('asdf', 'asdf');
in_array(new A, new B);
bcdiv('d', 1.2);
bcdiv();
sqrt('sdf');
typed_rpc_tl_query_one(1, 2, 3, 4, 5);
typed_rpc_tl_query_one();

// also don't report for @kphp-infer cast calls

/**
 * @kphp-infer cast
 * @param int $i
 * @param string $s
 */
function withCast($i, $s = '') {}

withCast(<error descr="No value passed for $i">)</error>;
withCast(1);
withCast('asdf');
withCast([], new A);
