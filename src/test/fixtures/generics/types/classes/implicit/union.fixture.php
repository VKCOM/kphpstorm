<?php

$a = mirror(new GlobalA() ?? new \Classes\A());
expr_type($a, "\Classes\A|\GlobalA");
