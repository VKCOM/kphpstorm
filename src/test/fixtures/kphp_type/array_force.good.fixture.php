<?php

class T {
  public $b = false;
  public $i = 1;
}

$t = new T;

$ints = [$t->i, $t->i];
$_int = $ints[0];

$bools = [$t->b, $t->b];
$_bool = $bools[0];

expr_type($ints[0], "int");
expr_type($_int, "int");
expr_type($bools[0], "bool");
expr_type($_bool, "bool");
