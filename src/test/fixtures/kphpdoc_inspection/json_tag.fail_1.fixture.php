<?php

class A {
  /**
   * <error descr="field A::$f1 is @kphp-json 'raw_string', but it's not a string">@kphp-json raw_string</error>
   */
  public $f1 = 0;

  /**
   * <error descr="field A::$f2 is @kphp-json 'raw_string', but it's not a string">@kphp-json raw_string</error>
   * <weak_warning descr="@var can be replaced with field hint 'float'">@var</weak_warning> float $f2
   */
  public $f2;

  /**
   * <error descr="field A::$f3 is @kphp-json 'raw_string', but it's not a string">@kphp-json raw_string</error>
   */
  public bool $f3;

  /**
   * <error descr="field A::$f4 is @kphp-json 'array_as_hashmap', but it's not a array">@kphp-json array_as_hashmap</error>
   */
  public $f4 = 0;

  /**
   * <error descr="field A::$f5 is @kphp-json 'array_as_hashmap', but it's not a array">@kphp-json array_as_hashmap</error>
   */
  public string $f5 = "";

  /**
   * <error descr="field A::$f6 is @kphp-json 'raw_string', but it's not a string">@kphp-json raw_string</error>
   */
  public $f6 = [1, 2, 3];

  /**
   * <error descr="field A::$f7 is @kphp-json 'raw_string', but it's not a string">@kphp-json raw_string</error>
   */
  public ?string $f7 = "";
}

/**
 * <error descr="@kphp-json 'float_precision' value should be non negative integer, got '-1'">@kphp-json float_precision=-1</error>
 */
class B {
  /**
   * <error descr="@kphp-json 'float_precision' value should be non negative integer, got '-2'">@kphp-json float_precision=-2</error>
   */
  public $f1 = 0;

  /**
   * <error descr="@kphp-json 'float_precision' value should be non negative integer, got 'value'">@kphp-json float_precision = value</error>
   */
  public $f2 = 0;
}

class C {
  /**
   * <error descr="@kphp-json is allowed only for instance fields: $f1">@kphp-json skip</error>
   */
  public static $f1 = 0;

  /**
   * <error descr="@kphp-json is allowed only for instance fields: $f2">@kphp-json unknown_field</error>
   */
  public static $f2 = 0;
}

/**
 * <error descr="Unknown @kphp-json tag 'unknown_class' above class D">@kphp-json unknown_class</error>
 */
class D {
  /**
   * <error descr="Unknown @kphp-json tag 'unknown_field' over class field D::$f1">@kphp-json unknown_field</error>
   */
  public $f1 = 0;
}

/**
 * <error descr="@kphp-json 'flatten' class 'E' must have exactly one field">@kphp-json flatten</error>
 */
class E {
}

/**
 * <error descr="@kphp-json 'flatten' class 'F' must have exactly one field">@kphp-json flatten</error>
 */
class F {
  public $f1 = 1;

  public $f2 = 2;
}

/**
 * <error descr="@kphp-json 'rename_policy' should be none|snake_case|camelCase, got 'unknown_value'">@kphp-json rename_policy=unknown_value</error>
 * <error descr="@kphp-json 'visibility_policy' should be all|public, got 'private'">@kphp-json visibility_policy=private</error>
 */
class G {
}

/**
 * <error descr="@kphp-json 'rename_policy' over a field is disallowed for flatten class 'H'">@kphp-json rename_policy=camelCase</error>
 * @kphp-json flatten
 * <error descr="@kphp-json 'visibility_policy' over a field is disallowed for flatten class 'H'">@kphp-json visibility_policy=all</error>
 */
class H {
  /**
   * <error descr="@kphp-json 'skip' over a field is disallowed for flatten class 'H'">@kphp-json skip</error>
   */
  public $f1 = 0;
}

/**
 * <error descr="@kphp-json 'float_precision' expected value">@kphp-json float_precision</error>
 * <error descr="@kphp-json 'rename_policy' expected value">@kphp-json rename_policy</error>
 * <error descr="@kphp-json 'visibility_policy' expected value">@kphp-json visibility_policy</error>
 */
class I {
  /**
   * <error descr="@kphp-json 'float_precision' expected value">@kphp-json float_precision</error>
   */
  public $f1 = 0;

  /**
   * <error descr="@kphp-json 'rename' expected value">@kphp-json rename</error>
   */
  public $f2 = "";
}

/**
 * <error descr="@kphp-json tag 'skip' is not applicable for the class J">@kphp-json skip</error>
 */
class J {
  /**
   * <error descr="@kphp-json tag 'rename_policy' is not applicable for the field J::$f1">@kphp-json rename_policy=snake_case</error>
   */
  public $f1 = 0;
}

/**
 * <error descr="@kphp-json 'flatten' should be empty or true|false, got 'disable'">@kphp-json flatten=disable</error>
 */
class K {
  /**
   * <error descr="@kphp-json 'skip_if_default' should be empty or true|false, got 'enable'">@kphp-json skip_if_default=enable</error>
   */
  public $f1 = 0;

  /**
   * <error descr="@kphp-json 'skip' should be empty or encode|decode|true|false, got '2'">@kphp-json skip = 2</error>
   */
  public $f3 = 123;

  /**
   * <error descr="@kphp-json 'skip' should be empty or encode|decode|true|false, got 'decoder'">@kphp-json skip = decoder</error>
   */
  public string $f4;
}

/**
 * <error descr="@kphp-json 'rename_policy' is duplicated">@kphp-json rename_policy=snake_case</error>
 * @kphp-json float_precision= 2
 * <error descr="@kphp-json 'rename_policy' is duplicated">@kphp-json rename_policy=camelCase</error>
 */
class L {
  /**
   * @kphp-json rename = name1
   * <error descr="@kphp-json 'skip_if_default' is duplicated">@kphp-json skip_if_default = true</error>
   * <error descr="@kphp-json 'skip_if_default' is duplicated">@kphp-json skip_if_default</error>
   */
  public $f1 = 0;
}

/**
 * @kphp-json fields=   <error descr="@kphp-json 'fields' specifies '$a', but such field doesn't exist in class M1">$a</error>
 */
class M1 {
  public int $f1 = 0;
}

/**
 * @kphp-json fields=<error descr="@kphp-json 'fields' specifies '$a', but such field doesn't exist in class M2">$a</error>, $f1
 */
class M2 {
  public int $f1 = 0;
}

/**
 * @kphp-json fields=$f1, <error descr="@kphp-json 'fields' specifies 'f1ы', but such field doesn't exist in class M3">f1</error>
 */
class M3 {
  public int $f1 = 0;
}
