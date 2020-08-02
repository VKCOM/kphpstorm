<?php

class ClassWithMethodsParametersTest {

    /**
     * @param type $arg_1
     * @param type $arg_2
     */
    public static function noCommentMethod(type $arg_1, type $arg_2) {

  }

  /**
   * comen
   * @param type $arg_1
   * @param type $arg_2
   */
  public static function kphpInferCommentMethod(type $arg_1, type $arg_2) {

  }

  /**
   * @param type $arg_1
   * @param type $arg_2
   */
  public static function emptyCommentMethod(type $arg_1, type $arg_2) {

  }

  /**
   * @param type $arg_1
   * @param type $arg_2
   * @param type $arg_3
   * @param
   * @param $arg_3
   * @param int $arg_4
   * @param Instance $arg_5
   */
  public static function manyParametersMethod(type $arg_1, type $arg_2, type $arg_3, $arg_4, $arg_5) {

  }

  /**
   * @param int $arg_2
   * @param Instance $arg_3
   */
  public static function allParametersTypedMethod(int $arg_1, $arg_2, $arg_3) {

  }

    /**
     * @param type $parameter_1
     */
    function splatOperatorMethod(type $parameter_1, ...$other_parameters) {

  }

  /**
   * @param int|false $a2
   * @param type $a3
   * @param string|false $a4
   */
  function insertToMiddle(int $a1, $a2, type $a3, $a4) {
  }
}

