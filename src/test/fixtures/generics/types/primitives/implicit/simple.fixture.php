<?php

$a = mirror("");
expr_type($a, "string");

$a1 = mirror(10);
expr_type($a1, "int");

$a2 = mirror(true);
expr_type($a2, "bool");
