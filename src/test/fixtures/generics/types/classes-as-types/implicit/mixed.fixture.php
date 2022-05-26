<?php

$a = mirror(tuple([new GlobalA()], new \Classes\A() ?? new \Classes\C));
// TODO: fix it
// expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");
