<?php

class A {

  const CONSTANT = 123;

    /** @var type */
    public type $field_without_phpdoc = 0;

  /**
   * @var type
   */
  static public type $field_with_empty_phpdoc = 0;

  /**
   * @var type
   * @var
   */
  public type $field_with_var_but_no_type_in_phpdoc = 0;

  /**
   * @var int
   */
  static public $properly_typed_simple_type_field;

  /**
   * @var Instance
   */
  public $properly_type_instance_field;

  public $int_with_default = 0;

  public $null_with_default = null;

  public $array_with_default = [];
}
