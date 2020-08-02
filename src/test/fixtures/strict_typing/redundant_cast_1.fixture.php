<?php

function getIntArr() { return [1,2,3]; }


function demo1(int $x) {
    $y1 = <warning descr="Redundant cast">(int)</warning>$x;
    $y2 = <warning descr="Redundant cast call">intval</warning>($x);
    $y3 = intval($x, 16);
}


/**
 * @param int[] $x
 */
function demo2($x) {
    $y1 = <warning descr="Redundant cast">(array)</warning>$x;
    $y2 = <warning descr="Redundant cast">(array)</warning>getIntArr();
}

/**
 * @param int[] | string[] $x
 */
function demo3($x) {
    $y1 = <warning descr="Redundant cast">(array)</warning>$x;
}

function demo4(?string $x) {
    $y1 = (string)$x;
    $y12 = (int)$x;
    if($x === null) return;
    $y2 = <warning descr="Redundant cast">(string)</warning>$x;
}
