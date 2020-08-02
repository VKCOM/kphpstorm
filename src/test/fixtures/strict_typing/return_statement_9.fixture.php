<?php

class A {
  /**
   * @param int|false $x
   * @return int
   */
  public static function demo($x) {
    return 5;
  }
}

class B extends A {
  public static function demo($x) {
    return 4;
  }
}

class B2 extends A {
  public static function demo($x) {
    return parent::demo($x);
  }
}

class C extends A {
  public static function demo($x) {
    <error descr="Can't return 'string', expected 'int'">return 'sdf';</error>
  }
}

class C2 extends C {
  public static function demo($x) {
    return 4;
  }
}

class C3 extends C {
  public static function demo($x) {
    <error descr="Can't return 'int|string', expected 'int'">return parent::demo($x);</error>
  }
}

class Deep1 extends A {}
class Deep2 extends Deep1 {}
class Deep3 extends Deep2 {
  public static function demo($x) {
    <error descr="Can't return 'null', expected 'int'">return null;</error>
  }
}


class Another {
    /** @return void */
    function f() {}
}

class BNother extends Another {
    function f() { <error descr="Can't return 'int', expected 'void'">return 4;</error> }
}


class Ex1 {
    static protected function f3(): int { return 4; }
}

class Ex2 extends Ex1 {
    static protected function f3() { <error descr="Can't return 'string', expected 'int'">return 'd';</error> }
}

class Ex3 extends Ex1 {
    static protected function f3(): string { return 's'; }
}
