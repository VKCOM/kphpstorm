<?php

use Classes\Base;
use Classes\Child1;
use Classes\Child2;

/**
 * @return Base[]
 */
function get_children() {
  return [new Child1, new Child2()];
}

expr_type(Child1::class, "class-string(\Classes\Child1)|string");

$base_array = get_children();
$children1_array = filter_is_instance/*<Base, Child1>*/($base_array, Child1::class);
expr_type($children1_array, "\Classes\Child1[]");
