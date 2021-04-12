<?php

/**
 *
 * @param
 * @param $arg_3
 * @param int $arg_4
 * @param Instance $arg_5
 */
function manyParametersNotTypedFunction(<error descr="Declare type hint or @param for this argument">$arg_1</error>, <error descr="Declare type hint or @param for this argument">$arg_2</error>, <error descr="Declare type hint or @param for this argument">$arg_3</error>, $arg_4, $arg_5) {

}


/**
 * @param int $arg_2
 * @param Instance $arg_3
 */
function allParametersTypedMethod(int $arg_1, $arg_2, $arg_3) {

}

/**
 * @param
 * @param $arg_3
 * @param int $arg_4
 * @param Instance $arg_5
 */
function manyParametersFunctionNotTyped(<error descr="Declare type hint or @param for this argument">$arg_1</error>, <error descr="Declare type hint or @param for this argument">$arg_2</error>, <error descr="Declare type hint or @param for this argument">$arg_3</error>, $arg_4, $arg_5) {

}

function splatOperatorFunction(<error descr="Declare type hint or @param for this argument">$parameter_1</error>, ...<error descr="Declare type hint or @param for this argument">$other_parameters</error>) {

}

/**
 */
function withDefaultValue(<error descr="Declare type hint or @param for this argument">$a1</error>, $a2 = false, $a3 = 0) {
}

/**
 * @param mixed $result
 */
function mixedWithTypeHints(string $key, $result, bool $shouldCompare, <error descr="Declare type hint or @param for this argument">$kphp_result</error>) {
}
