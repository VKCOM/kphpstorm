<?php

// \GlobalA
// \Classes\A
use Classes\B;
use Classes\C as GlobalC;
use Classes\D as GlobalD;

// \GlobalD

// Класс из глобального скоупа + Класс из пространства имен
$a = mirror/*<shape(key1: GlobalA, key2: \Classes\A)>*/ (shape(["key1" => new GlobalA, "key2" => new \Classes\A]));
expr_type($a, "shape(key1:\GlobalA,key2:\Classes\A)");

// Импортированный класс из пространства имен + Импортированный класс из пространства имен с алиасом
$a = mirror/*<shape(key1: B, key2: GlobalC)>*/ (shape(["key1" => new B(), "key2" => new GlobalC()]));
expr_type($a, "shape(key1:\Classes\B,key2:\Classes\C)");

// Импортированный класс из пространства имен с алиасом как у глобально класса + Глобальный класс с именем как у локального алиаса для другого класса
$a = mirror/*<shape(key1: GlobalD, key2: \GlobalD)>*/ (shape(["key1" => new GlobalD(), "key2" => new \GlobalD()]));
expr_type($a, "shape(key1:\Classes\D,key2:\GlobalD)");
