<?php

namespace Fields\Static;

/**
 * @kphp-generic T
 */
class GenericClass {
  /** @var <error descr="Undefined class 'T'">T</error> */
  public static $static_field = null;
}

expr_type(GenericClass::$static_field, "?\Fields\Static\T");
