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

$base_array = get_children();
$children2_array = filter_is_instance($base_array, Child2::class);
expr_type($children2_array, "\Classes\Child2[]");
