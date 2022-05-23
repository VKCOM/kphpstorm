<?php

$a = mirror/*<tuple(GlobalA[], \Classes\A|GlobalC)>*/(tuple([new GlobalA()], new \Classes\A()));
expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");

$a1 = mirror/*<shape(key1: tuple(GlobalA[], \Classes\A|GlobalC), key2: ?\GlobalD[])>*/(shape(["key1" => $a, "key2" => [new \GlobalD()]]));
expr_type($a1, "shape(key1:tuple(\GlobalA[],\Classes\C|\Classes\A),key2:?\GlobalD[])");
