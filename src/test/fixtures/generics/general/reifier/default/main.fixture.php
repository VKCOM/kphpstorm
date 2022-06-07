<?php

namespace Reifier\Default;

class Pair {}
/** @kphp-generic T */
class Vector {}

/**
 * @kphp-generic T = Pair
 * @return T
 */
function singleDefaultType() { return null; }

$a = singleDefaultType/*<Pair>*/();
expr_type($a, "\Reifier\Default\Pair");

$b = singleDefaultType();
expr_type($b, "\Reifier\Default\Pair");


/**
 * @kphp-generic T1 = Pair, T2 = Vector
 * @return T1|T2
 */
function twoDefaultType() { return null; }

$a = twoDefaultType/*<Pair, Vector>*/();
expr_type($a, "\Reifier\Default\Pair|\Reifier\Default\Vector");

$b = twoDefaultType/*<Pair>*/();
expr_type($b, "\Reifier\Default\Pair|\Reifier\Default\Vector");

$c = twoDefaultType();
expr_type($c, "\Reifier\Default\Pair|\Reifier\Default\Vector");


/**
 * @kphp-generic T1, T2 = Pair
 * @param T1 $a
 * @return T1|T2
 */
function singleDefaultTypeAndSecondFromParam($a) { return null; }

$a = singleDefaultTypeAndSecondFromParam(new Vector/*<string>*/());
expr_type($a, "\Reifier\Default\Pair|\Reifier\Default\Vector(string)");
