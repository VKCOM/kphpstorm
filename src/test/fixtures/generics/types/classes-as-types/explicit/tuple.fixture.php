<?php

// \GlobalA
// \Classes\A
use Classes\B;
use Classes\C as GlobalC;
use Classes\D as GlobalD;

// \GlobalD

// Класс из глобального скоупа + Класс из пространства имен
$a = mirror/*<tuple(GlobalA, \Classes\A)>*/ (tuple(new GlobalA(), new \Classes\A()));
expr_type($a, "tuple(\GlobalA,\Classes\A)");

// Импортированный класс из пространства имен + Импортированный класс из пространства имен с алиасом
$a = mirror/*<tuple(B, GlobalC)>*/ (tuple(new B(), new GlobalC()));
expr_type($a, "tuple(\Classes\B,\Classes\C)");

// Импортированный класс из пространства имен с алиасом как у глобально класса + Глобальный класс с именем как у локального алиаса для другого класса
$a = mirror/*<tuple(GlobalD, \GlobalD)>*/ (tuple(new GlobalD(), new \GlobalD()));
expr_type($a, "tuple(\Classes\D,\GlobalD)");
