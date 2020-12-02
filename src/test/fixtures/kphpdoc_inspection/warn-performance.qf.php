<?php

/**
 * @kphp-warn-performance
 * @kphp-analyze-performance
 */
function demo1() {}

/**
 * @kphp-analyze-performance all
 * @kphp-inline
 */
function demo2() {}

/**
 * @kphp-warn-performance all
 */
function demo3() {}

/**
 * @kphp-warn-performance all
 */
function demo4() {}

/**
 * @kphp-warn-performance all !array-merge-into !implicit-array-cast
 */
function demo5() {}

/**
 * @kphp-warn-performance array-merge-into
 */
function demo6() {}

/**
 * @kphp-warn-performance !array-merge-into
 */
function demo7() {}

/**
 * @kphp-warn-performance !array-merge-into all
 */
function demo8() {}

/**
 * @kphp-warn-performance !all
 */
function demo9() {}

/**
 * @kphp-warn-performance all
 */
function demo10() {}

/**
 * @kphp-warn-performance implicit-array-cast  array-merge-into array-reserve
 */
function demo11() {}
