<?php

interface I1 {}

class Grand {}
class Par extends Grand {}
class Child extends Par implements I1 {}

function getG1() : ?Grand {
    return new Grand();
}

function getG2() : Grand {
    return new Grand();
}

function getG3() : ?Grand {
    return new Child();
}

/** @return Grand|null */
function getG4() {
    return null;
}

/** @return Grand */
function getG5() {
    return 1 ? new Par : new Grand;
}

function getI1() : ?I1 {
    <error descr="Can't return 'Par', expected '?I1'">return new Par;</error>
}

function getI2() : ?I1 {
    return 1 ? null : new Child;
}

function getP1() : Par {
    return new Par;
}

function getP2() : Par {
    <error descr="Can't return 'Grand', expected 'Par'">return new Grand();</error>
}

function getP3() : Par {
    return <error descr="Cannot return 'null': return type is non-nullable">null</error>;
}

/** @return Par */
function getP3_v2() {
    return null;
}

function getP4() : Par {
    $a = new Grand;
    if($a instanceof Par)
        return $a;
    <error descr="Can't return 'Grand', expected 'Par'">return $a;</error>
}

function getP5() : ?Par {
    <error descr="Can't return 'Par[]', expected '?Par'">return [new Par];</error>
}

function getP6() : Par {
    <error descr="Can't return '?I1', expected 'Par'">return getI1();</error>
}

function getO() : object {
    return new Par;
}



