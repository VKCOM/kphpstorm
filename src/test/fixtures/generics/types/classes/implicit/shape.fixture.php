<?php

$a = mirror(shape(["key1" => new GlobalA, "key2" => new \Classes\A]));
// TODO: fixed any[]
// expr_type($a, "shape(key1:\GlobalA,key2:\Classes\A)");
