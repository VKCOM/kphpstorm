<?php

/**
 */
function <error descr="Declare return hint or @return in phpdoc">functionWithEmptyPhpDoc</error>() {
  return true;
}

/**
 * @return bool
 */
function phpDocTypeDeclaredFunction() {
  return true;
}

function typeDeclaredFunction(): int {
  return 1;
}

/**
 * @return false
 */
function noKphpInferPhpDocTypeDeclaredFunction() {
  return 1;
}

function <error descr="Declare return hint or @return in phpdoc">noKphpInferNoTypeDeclaredFunction</error>() {
  return 1;
}
