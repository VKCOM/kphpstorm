<?php

function getIntArr() { return [1,2,3]; }


function demo1(int $x) {
    $y1 = $x;
    $y2 = $x;
    $y3 = intval($x, 16);
}


/**
 * @param int[] $x
 */
function demo2($x) {
    $y1 = $x;
    $y2 = getIntArr();
}

/**
 * @param int[] | string[] $x
 */
function demo3($x) {
    $y1 = $x;
}

function demo4(?string $x) {
    $y1 = (string)$x;
    $y12 = (int)$x;
    if($x === null) return;
    $y2 = $x;
}

function demo5() {
  $ids = [];
  if (rand())
    $ids = [1,2,3];
  foreach ($ids as $id) {
    $id2 = (int)$id;  // no warning, as $id is inferred as int|any
  }
}
