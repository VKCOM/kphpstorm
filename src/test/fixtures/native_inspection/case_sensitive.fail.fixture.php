<?php

class AbClass {
  public function method() {}
}

$cls = new <error descr="Case in class usage doesn't match the case in declaration">aBclass</error>();

function AbFun() {}

$_ = <error descr="Case in function/method call doesn't match the case in declaration">aBfuN</error>();

$_ = $cls-><error descr="Case in function/method call doesn't match the case in declaration">Method</error>();
