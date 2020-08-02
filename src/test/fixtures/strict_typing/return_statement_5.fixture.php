<?

/**
 * @kphp-template $i
 * @param object $i
 * <error descr="Actual return type is 'void'">@return</error> object
 */
function returnI($i) {
}

/**
 * @kphp-template $i
 * @param object $i
 * <error descr="Actual return type is 'void'">@return</error> AAA          !! this is incorrect! no matter that @kphp-template is written, PhpStorm takes no account of it
 */
function returnInvalid($i) {
}


/** @return AAA */
function demo1() {
  return returnI(new AAA);
}

/** @return AAA */
function demo2() {
  return returnI(new AAADerived());
}

/** @return AAADerived */
function demo3() {
  return returnI(new AAADerived());
}

/** @return AAA */
function demo1_2() {
  return returnInvalid(new AAA);
}

/** @return AAA */
function demo2_2() {
  return returnInvalid(new AAADerived());
}

/** <error descr="Actual return type is 'AAA'">@return</error> AAADerived */
function demo3_2() {
    // @return AAA gives this error
  <error descr="Can't return 'AAA', expected 'AAADerived'">return returnInvalid(new AAADerived());</error>
}
