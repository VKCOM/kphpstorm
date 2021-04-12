<?php

/**
 *
 * @param type $arg_1
 * @param type $arg_2
 * @param type $arg_3
 * @param
 * @param $arg_3
 * @param int $arg_4
 * @param Instance $arg_5
 */
function manyParametersNotTypedFunction(type $arg_1, type $arg_2, type $arg_3, $arg_4, $arg_5) {

}


/**
 * @param int $arg_2
 * @param Instance $arg_3
 */
function allParametersTypedMethod(int $arg_1, $arg_2, $arg_3) {

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
function manyParametersFunctionNotTyped(type $arg_1, type $arg_2, type $arg_3, $arg_4, $arg_5) {

}

/**
 * @param type $parameter_1
 * @param type $other_parameters
 */
function splatOperatorFunction(type $parameter_1, type...$other_parameters) {

}

/**
 * @param type $a1
 */
function withDefaultValue(type $a1, $a2 = false, $a3 = 0) {
}

/**
 * @param mixed $result
 * @param type $kphp_result
 */
function mixedWithTypeHints(string $key, $result, bool $shouldCompare, type $kphp_result) {
}
