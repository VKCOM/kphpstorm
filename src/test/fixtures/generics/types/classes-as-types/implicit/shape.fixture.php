<?php

// TODO: fixed it
$a = mirror(shape(["key1" => new GlobalA, "key2" => new \Classes\A]));
 expr_type($a, "shape(key1:\GlobalA,key2:\Classes\A)");
