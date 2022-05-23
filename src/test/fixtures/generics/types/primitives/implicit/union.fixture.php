<?php

$a = combine("", 1);
expr_type($a, "int|string");

$a1 = combine("", true);
expr_type($a1, "bool|string");

$a2 = combine("", false);
expr_type($a2, "false|string");

$a3 = combine("", true);
expr_type($a3, "bool|string");

$a3 = combine("", "");
expr_type($a3, "string");
