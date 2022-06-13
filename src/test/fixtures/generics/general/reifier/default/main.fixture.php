<?php

namespace Reifier\Default;

/** @kphp-generic T1, T2 */
class Pair {}
/** @kphp-generic T */
class Vector {}

/**
 * @kphp-generic T = Pair
 * @return T
 */
function singleDefaultType() { return null; }

$a = singleDefaultType/*<Pair<int, string>>*/();
expr_type($a, "\Reifier\Default\Pair|\Reifier\Default\Pair(int,string)");

$b = singleDefaultType();
expr_type($b, "\Reifier\Default\Pair");


/**
 * @kphp-generic T1 = Pair, T2 = Vector
 * @return T1|T2
 */
function twoDefaultType() { return null; }

$a = twoDefaultType/*<Pair<int, string>, Vector<string>>*/();
expr_type($a, "\Reifier\Default\Pair|\Reifier\Default\Pair(int,string)|\Reifier\Default\Vector|\Reifier\Default\Vector(string)");

$b = twoDefaultType/*<Pair<int, string>>*/();
expr_type($b, "\Reifier\Default\Pair|\Reifier\Default\Pair(int,string)|\Reifier\Default\Vector");

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
