<?php

/**
 * <error descr="Provide arguments: 'all' or detailed inspections">@kphp-warn-performance</error>
 * <error descr="Both warn and analyze annotations exist">@kphp-analyze-performance</error>
 */
function demo1() {}

/**
 * @kphp-analyze-performance all
 * @kphp-inline
 */
function demo2() {}

/**
 * @kphp-warn-performance all <warning descr="Unknown item">adsf</warning>
 */
function demo3() {}

/**
 * @kphp-warn-performance all <warning descr="Unknown item">array-merge</warning>
 */
function demo4() {}

/**
 * @kphp-warn-performance all !array-merge-into !implicit-array-cast <warning descr="Unknown item">!asdf</warning>
 */
function demo5() {}

/**
 * @kphp-warn-performance array-merge-into <warning descr="Duplicate item">array-merge-into</warning>
 */
function demo6() {}

/**
 * @kphp-warn-performance <warning descr="Using negation without 'all' is confusing">!array-merge-into</warning>
 */
function demo7() {}

/**
 * @kphp-warn-performance <warning descr="Using negation without 'all' is confusing">!array-merge-into</warning> <error descr="Use 'all' at the beginning">all</error>
 */
function demo8() {}

/**
 * @kphp-warn-performance <error descr="All can't be negated">!all</error>
 */
function demo9() {}

/**
 * @kphp-warn-performance all <warning descr="'all' exists, this item has no effect">implicit-array-cast</warning>
 */
function demo10() {}

/**
 * @kphp-warn-performance implicit-array-cast <warning>array-unknown</warning> array-merge-into array-reserve
 */
function demo11() {}
