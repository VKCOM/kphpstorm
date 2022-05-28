<?php

// TODO: fix it
$a = mirror(tuple([new GlobalA()], new \Classes\A() ?? new \Classes\C));
 expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");
