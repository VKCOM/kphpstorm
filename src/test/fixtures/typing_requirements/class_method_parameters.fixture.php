<?php

class ClassWithMethodsParametersTest {

  public static function noCommentMethod(<error descr="Declare type hint or @param for this argument">$arg_1</error>, <error descr="Declare type hint or @param for this argument">$arg_2</error>) {

  }

  /**
   * comen
   */
  public static function kphpInferCommentMethod(<error descr="Declare type hint or @param for this argument">$arg_1</error>, <error descr="Declare type hint or @param for this argument">$arg_2</error>) {

  }

  /**
   */
  public static function emptyCommentMethod(<error descr="Declare type hint or @param for this argument">$arg_1</error>, <error descr="Declare type hint or @param for this argument">$arg_2</error>) {

  }

  /**
   * @param
   * @param $arg_3
   * @param int $arg_4
   * @param Instance $arg_5
   */
  public static function manyParametersMethod(<error descr="Declare type hint or @param for this argument">$arg_1</error>, <error descr="Declare type hint or @param for this argument">$arg_2</error>, <error descr="Declare type hint or @param for this argument">$arg_3</error>, $arg_4, $arg_5) {

  }

  /**
   * @param int $arg_2
   * @param Instance $arg_3
   */
  public static function allParametersTypedMethod(int $arg_1, $arg_2, $arg_3) {

  }

  function splatOperatorMethod(<error descr="Declare type hint or @param for this argument">$parameter_1</error>, ...<error descr="Declare type hint or @param for this argument">$other_parameters</error>) {

  }

  /**
   * @param int|false $a2
   * @param string|false $a4
   */
  function insertToMiddle(int $a1, $a2, <error descr="Declare type hint or @param for this argument">$a3</error>, $a4) {
  }
}

