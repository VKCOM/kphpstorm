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
