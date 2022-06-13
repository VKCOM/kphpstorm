<?php

$a = mirror(tuple(new GlobalA(), new \Classes\A()));
expr_type($a, "tuple(\GlobalA,\Classes\A)");

$a = mirror(tuple(new GlobalA()));
expr_type($a, "tuple(\GlobalA)");
