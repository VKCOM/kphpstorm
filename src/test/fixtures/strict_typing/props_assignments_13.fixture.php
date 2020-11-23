<?php

class A {
  /** @var int */
  public $i;

  public $s = '';
}

/** @return mixed */
function getMixed() { return null; }

function demo1() {
  $m = getMixed();
  $a = new A;
  // foreach on mixed is mixed (not undefined, not any)
  foreach ($m as $sub) {
    <error descr="Can't assign 'mixed' to 'int' $i">$a->i = $sub</error>;
  }
}


function getUntypedArray(): array { return []; }

/** @return int[] */
function getIntArray(): array { return []; }

/** @return string[] */
function getStringArray(): array { return []; }

function demo2() {
  $a = new A;

  $ip1 = getUntypedArray()['asdf'];
  if (rand()) {
    $ip1 = 'string';
  }
  <error descr="Can't assign 'string|any' to 'int' $i">$a->i = $ip1</error>;
  $a->s = $ip1;

  $ip2 = getIntArray()['asdf'];
  if (rand()) {
    $ip2 = 'string';
  }
  <error descr="Can't assign 'int|string' to 'string' $s">$a->s = $ip2</error>;

  $ip3 = getStringArray()['asdf'];
  if (rand()) {
    $ip3 = 'string';
  }
  $a->s = $ip3;
}
