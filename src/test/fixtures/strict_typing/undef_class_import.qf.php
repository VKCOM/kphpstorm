<?php

use LocalNS1\InnerClass1;
use LocalNS2\InnerClass2;
use LocalNS2\InnerClass22;

namespace LocalNS1 {
  class InnerClass1 {}
}

namespace LocalNS2 {
  class InnerClass2 {}
  class InnerClass22 {}
}

/**
 * @param InnerClass1 $p
 */
function demo($p, InnerClass2 $p2) {
  InnerClass22::f();
  UnknownCl::f();
}
