<?php

class A {
  /**
   * <error descr="@kphp-json raw_string tag is allowed only above string type, got above $f1 field">@kphp-json raw_string</error>
   */
  public $f1 = 0;

  /**
   * <error descr="@kphp-json raw_string tag is allowed only above string type, got above $f2 field">@kphp-json raw_string</error>
   * <weak_warning descr="@var can be replaced with field hint 'float'">@var</weak_warning> float $f2
   */
  public $f2;

  /**
   * <error descr="@kphp-json raw_string tag is allowed only above string type, got above $f3 field">@kphp-json raw_string</error>
   */
  public bool $f3;

  /**
   * <error descr="@kphp-json array_as_hashmap tag is allowed only above array type, got above $f4 field">@kphp-json array_as_hashmap</error>
   */
  public $f4 = 0;

  /**
   * <error descr="@kphp-json array_as_hashmap tag is allowed only above array type, got above $f5 field">@kphp-json array_as_hashmap</error>
   */
  public string $f5 = "";

  /**
   * <error descr="@kphp-json raw_string tag is allowed only above string type, got above $f6 field">@kphp-json raw_string</error>
   */
  public $f6 = [1, 2, 3];
}

/**
 * <error descr="@kphp-json 'float_precision' value should be non negative integer, got '-1'">@kphp-json float_precision=-1</error>
 */
class B {
  /**
   * <error descr="@kphp-json 'float_precision' value should be non negative integer, got '-2'">@kphp-json float_precision=-2</error>
   */
  public $f1 = 0;
}

class C {
  /**
   * <error descr="@kphp-json is allowed only for instance fields: $f1">@kphp-json skip</error>
   */
  public static $f1 = 0;
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
 * <error descr="@kphp-json 'flatten' tag is allowed only for class with a single field, class name E">@kphp-json flatten</error>
 */
class E {
}

/**
 * <error descr="@kphp-json 'flatten' tag is allowed only for class with a single field, class name F">@kphp-json flatten</error>
 */
class F {
  public $f1 = 1;

  public $f2 = 2;
}

class G {
  /**
   * @kphp-json float_precision=1
   * <error descr="@kphp-json 'skip' can't be used together with other @kphp-json tags">@kphp-json skip</error>
   */
  public $f1 = 1;
}

/**
 * <error descr="@kphp-json 'fields_rename' should be either none|snake_case|camelCase">@kphp-json fields_rename=unknown_value</error>
 * <error descr="@kphp-json 'fields_visibility' should be either all|public">@kphp-json fields_visibility=private</error>
 * <error descr="@kphp-json 'skip_if_default' should be either true|false">@kphp-json skip_if_default=unknown_value</error>
 */
class H {
  /**
   * <error descr="@kphp-json 'skip_if_default' should be either true|false">@kphp-json skip_if_default=private</error>
   */
  public $f1 = 0;
}

/**
 * <error descr="'fields_rename' can't be used for a @kphp-json 'flatten' class">@kphp-json fields_rename=camelCase</error>
 * @kphp-json flatten
 * <error descr="'skip_if_default' can't be used for a @kphp-json 'flatten' class">@kphp-json skip_if_default=true</error>
 */
class I {
  /**
   * <error descr="'skip' can't be used for a @kphp-json 'flatten' class">@kphp-json skip</error>
   */
  public $f1 = 0;
}

/**
 * <error descr="@kphp-json 'float_precision' expected value">@kphp-json float_precision</error>
 * <error descr="@kphp-json 'fields_rename' expected value">@kphp-json fields_rename</error>
 * <error descr="@kphp-json 'fields_visibility' expected value">@kphp-json fields_visibility</error>
 * <error descr="@kphp-json 'skip_if_default' expected value">@kphp-json skip_if_default</error>
 */
class J {
  /**
   * <error descr="@kphp-json 'float_precision' expected value">@kphp-json float_precision</error>
   */
  public $f1 = 0;

  /**
   * <error descr="@kphp-json 'skip_if_default' expected value">@kphp-json skip_if_default</error>
   */
  public $f2 = 0;
}
