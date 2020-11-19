<?php

namespace LocalNS1 {
  class InnerClass1 {}
}

namespace LocalNS2 {
  class InnerClass2 {}
  class InnerClass22 {}
}

/**
 * @param <error descr="Undefined class 'InnerClass1'">InnerClass1</error> $p
 */
function demo($p, <error descr="Undefined class 'InnerClass2'">InnerClass2</error> $p2) {
  <error descr="Undefined class 'InnerClass22'">InnerClass22</error>::f();
  <error descr="Undefined class 'UnknownCl'">UnknownCl</error>::f();
}
