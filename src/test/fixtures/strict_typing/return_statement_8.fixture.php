<?php

function demo1() {
    <error descr="Can't return 'int', expected 'void'">return 123;</error>
}

/**
 * But with kphp-infer return type assumed void
 */
function demo2() {
    <error descr="Can't return 'int', expected 'void'">return 123;</error>
    <error descr="Can't return 'string', expected 'void'">return '123';</error>
    return;
}

function demo3(int $a) {
    <error descr="Can't return 'int', expected 'void'">return 123;</error>
    return;
}

function demo4(): void {
    <error descr="Can't return 'int', expected 'void'">return 123;</error>
}
