<?php

class A {

  const CONSTANT = 123;

  public <error descr="All fields must have @var phpdoc or default value">$field_without_phpdoc</error>;

  /**
   */
  static public <error descr="All fields must have @var phpdoc or default value">$field_with_empty_phpdoc</error>;

  /**
   * @var
   */
  public <error descr="All fields must have @var phpdoc or default value">$field_with_var_but_no_type_in_phpdoc</error>;

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
