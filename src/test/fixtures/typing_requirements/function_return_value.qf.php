<?php

/**
 * @return type
 */
function functionWithEmptyPhpDoc(): type
{
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

/**
 * @return type
 */
function noKphpInferNoTypeDeclaredFunction(): type
{
  return 1;
}
