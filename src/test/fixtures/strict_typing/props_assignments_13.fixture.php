<?php

class A {
  /** @var int */
  public $i;
}

/** @return mixed */
function getMixed() { return null; }

function demo1() {
  $m = getMixed();
  $a = new A;
  // foreach on mixed is mixed (not undefined, not any)
  foreach ($m as $sub) {
    <error descr="Can't assign 'var' to 'int' $i">$a->i = $sub</error>;
  }
}
