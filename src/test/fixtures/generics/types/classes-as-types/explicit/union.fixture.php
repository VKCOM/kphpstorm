<?php

// \GlobalA
// \Classes\A
use Classes\B;
use Classes\C as GlobalC;
use Classes\D as GlobalD;

// \GlobalD

// Класс из глобального скоупа + Класс из пространства имен
$a = mirror/*<GlobalA|\Classes\A>*/ (new GlobalA());
expr_type($a, "\Classes\A|\GlobalA");

// Импортированный класс из пространства имен + Импортированный класс из пространства имен с алиасом
$a = mirror/*<B|GlobalC>*/ (new B());
expr_type($a, "\Classes\B|\Classes\C");

// Импортированный класс из пространства имен с алиасом как у глобально класса + Глобальный класс с именем как у локального алиаса для другого класса
$a = mirror/*<GlobalD|\GlobalD>*/ (new GlobalD());
expr_type($a, "\Classes\D|\GlobalD");
