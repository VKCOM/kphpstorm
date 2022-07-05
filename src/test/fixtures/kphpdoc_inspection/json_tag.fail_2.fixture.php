<?php

class GuestJsonEncoder {
}

class AdminJsonEncoder {
}

/**
 * @kphp-json rename_policy=none
 * <error descr="@kphp-json 'rename_policy' is duplicated">@kphp-json for GuestJsonEncoder rename_policy=camelCase</error>
 * <error descr="@kphp-json 'rename_policy' is duplicated">@kphp-json for GuestJsonEncoder rename_policy=camelCase</error>
 */
class A {
  /**
   * @kphp-json float_precision = 3
   * <error descr="@kphp-json 'float_precision' is duplicated">@kphp-json for AdminJsonEncoder float_precision = 5</error>
   * <error descr="@kphp-json 'float_precision' is duplicated">@kphp-json for AdminJsonEncoder float_precision = 2</error>
   */
  public $f1 = 123.4567;
}

/**
 * <error descr="@kphp-json for AdminJsonEncoder 'fields' should be placed below @kphp-json 'fields' without for">@kphp-json for AdminJsonEncoder fields = f1</error>
 * <error descr="@kphp-json for GuestJsonEncoder 'fields' should be placed below @kphp-json 'fields' without for">@kphp-json for GuestJsonEncoder fields = f2</error>
 *
 * @property-read $f1
 *
 * @kphp-json rename_policy=snake_case

 * @kphp-json fields = f1
 */
class B {
  public string $f1 = "";

  public bool $f2 = true;
}

/**
 * <error descr="@kphp-json 'flatten' can't be used with 'for', it's a global state">@kphp-json for AdminJsonEncoder flatten</error>
 */
class C {
  private float $f1 = 11.11;
}
