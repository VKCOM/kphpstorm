<?php

namespace Fields\Static;

/**
 * @kphp-generic T
 */
class GenericClass {
  /** @var T */
  public static $static_field = null;
}

// Static fields not supported.
expr_type(GenericClass::$static_field, "null");
