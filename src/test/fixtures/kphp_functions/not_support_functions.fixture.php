<?php

function demo1() {
    $arr = [];

    <error descr="KPHP does not support end(), maybe use array_last_value()?">end($arr)</error>;
}

function demo2() {
    $arr = [];

    <error descr="KPHP does not support reset(), maybe use array_first_value()?">reset($arr)</error>;
}
