<?php

class ClassMethodReturnTest {

  public static function <error descr="Declare return hint or @return in phpdoc">methodWithoutPhpDoc</error>() {
    return true;
  }

  /**
   */
  public static function <error descr="Declare return hint or @return in phpdoc">methodWithEmptyPhpDoc</error>() {
    return true;
  }

  /**
   * @return bool
   */
  public static function phpDocTypeDeclaredMethod() {
    return true;
  }

  public static function typeDeclaredMethod(): bool {
    return true;
  }

  public static function typeDeclaredMethod2(): bool {
    return 1;
  }
}

class A {
    /** @return int */
    static function f1() { return 5; }

    function f2(): int { return 5; }

    static function f3() {}

    /** @return void */
    static function f4() {}
}

class B extends A {
    static function f1() { return 6; }

    function f2() { return 6; }

    static function <error descr="Declare return hint or @return in phpdoc">f3</error>() { return 1; }

    static function f4() { return 2; }
}
