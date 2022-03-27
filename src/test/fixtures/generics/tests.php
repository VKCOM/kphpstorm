<?php
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */


/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function nullable($arg) {
    if (0) return null;
    return $arg;
}

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror($arg) {
    return $arg;
}
//
//$a = mirror(tuple([new GlobalA()], new \Classes\A() ?? new \Classes\C));
//expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");
//

class AAA {
    /**
     * @kphp-generic T
     * @param T $arg
     * @return T
     */
    function mirror($arg) {
        return $arg;
    }

    /**
     * @kphp-generic T1, T2
     * @param T1 $arg
     * @param T2 $arg2
     * @return T1|T2
     */
    function combine($arg, $arg2) {
        return $arg;
    }
}

//$a = new Vector(100);

//$a1 = new Vector("");
//$res = $a->combine_with/*<string>*/($a1);

$aaa = new AAA();
//$aaa->mirror/*<string>*/("");

$aaa->combine/*<string, Vector<string>>*/("", new Vector(""));






