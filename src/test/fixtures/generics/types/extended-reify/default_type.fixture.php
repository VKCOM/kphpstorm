<?php

/**
 * @kphp-generic T = Pair
 * @return T
 */
function singleDefaultType() { return null; }

$a = singleDefaultType/*<Pair>*/();
expr_type($a, "\Pair");

$b = singleDefaultType();
expr_type($a, "\Pair");


/**
 * @kphp-generic T1 = Pair, T2 = Vector
 * @return T1|T2
 */
function twoDefaultType() { return null; }

$a = twoDefaultType/*<Pair, Vector>*/();
expr_type($a, "\Pair|\Vector");

$b = twoDefaultType();
expr_type($a, "\Pair|\Vector");


/**
 * @kphp-generic T1 = Pair, T2
 * @param T2 $a
 * @return T1|T2
 */
function singleDefaultTypeAndSecondFromParam($a) { return null; }

$a = singleDefaultTypeAndSecondFromParam(new Vector/*<string>*/());
expr_type($a, "\Pair|\Vector(string)");
