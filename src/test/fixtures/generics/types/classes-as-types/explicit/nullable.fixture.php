<?php

// \GlobalA
// \Classes\A
use Classes\B;
use Classes\C as GlobalC;
use Classes\D as GlobalD;

// \GlobalD

// Класс из глобального скоупа
$a = mirror/*<?GlobalA>*/ (null);
$a->methodGlobalA();


expr_type($a, "?\GlobalA");

// Класс из пространства имен
$a = mirror/*<?\Classes\A>*/ (new \Classes\A());
expr_type($a, "?\Classes\A");

// Импортированный класс из пространства имен
$a = mirror/*<?B>*/ (new B());
expr_type($a, "?\Classes\B");

// Импортированный класс из пространства имен с алиасом
$a = mirror/*<?GlobalC>*/ (new GlobalC);
expr_type($a, "?\Classes\C");

// Импортированный класс из пространства имен с алиасом как у глобально класса
$a = mirror/*<?GlobalD>*/ (new GlobalD());
expr_type($a, "?\Classes\D");

// Глобальный класс с именем как у локального алиаса для другого класса
$a = mirror/*<?\GlobalD>*/ (new \GlobalD());
expr_type($a, "?\GlobalD");
