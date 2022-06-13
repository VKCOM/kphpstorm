<?php

$a = combine/*<string, int>*/("", 1);
expr_type($a, "int|string");

$a1 = combine/*<string, bool>*/("", true);
expr_type($a1, "bool|string");

$a2 = combine/*<string, false>*/("", false);
expr_type($a2, "false|string");

$a3 = combine/*<string, boolean>*/("", true);
expr_type($a3, "bool|string");

$a3 = combine/*<string, string>*/("", "");
expr_type($a3, "string");
