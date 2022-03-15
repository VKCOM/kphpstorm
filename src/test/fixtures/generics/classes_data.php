<?php

namespace Classes {
  class A {}
  class B {}
  class C {}
  class D {}

  class Child1 extends Base {}
  class Child2 extends Base {}
  class Base {}
}

namespace {
  class GlobalA {
      function methodGlobalA() {}
  }
  class GlobalD {}
}
