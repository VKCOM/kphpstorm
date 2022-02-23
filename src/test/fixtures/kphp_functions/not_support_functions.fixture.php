<?php

function demo1() {
    $arr = [];

    <error descr="KPHP does not support end()">end($arr)</error>;
}

function demo2() {
    $arr = [];

    <error descr="KPHP does not support reset()">reset($arr)</error>;
}

function demo3() {
    $arr = [];

    <error descr="KPHP does not support reset()">\reset($arr)</error>;
}
