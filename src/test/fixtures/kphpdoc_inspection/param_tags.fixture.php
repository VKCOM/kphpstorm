<?php

use VK\API\Builders\MsgBuilder;

/**
 * <warning descr="@kphp-infer is deprecated">@kphp-infer</warning>
 * <weak_warning descr="@param can be replaced with type hint 'int'">@param</weak_warning> int $s
 * <error descr="@param tag for unexisting argument">@param</error> $asdf int comment
 */
function withIncorrectParam($s, $i) {
}

/**
 * <weak_warning descr="@param can be replaced with type hint 'int'">@param</weak_warning> int $s
 * @param int ...$ints
 */
function withVarargParam1($s, ...$ints) {
}

/**
 * <weak_warning descr="@param can be replaced with type hint 'int'">@param</weak_warning> int $s
 * @param int[] $ints
 */
function withVarargParam2($s, ...$ints) {
}

/**
 * <weak_warning descr="@param just duplicates type hint">@param</weak_warning> int $s
 * @param int|false $i_f
 */
function withDuplicatingParam1(int $s, $i_f) {
}

/**
 * <weak_warning descr="@param just duplicates type hint">@param</weak_warning> int $s
 * <error descr="@param mismatches type hint">@param</error> int|false $i_f
 * <error descr="@param mismatches type hint">@param</error> ?string $s_n
 */
function withDuplicatingParam2(int $s, int $i_f, string $s_n) {
}

/**
 * <weak_warning descr="@param just duplicates type hint">@param</weak_warning> int $s
 * @param int[] $arr
 */
function withDuplicatingParam3(int $s, array $arr) {
}

/**
 * @param int $i comment
 * @param string $s comment
 */
function withDuplicatingParam4(int $i, string $s) {
}

/**
 * <weak_warning descr="@param just duplicates type hint">@param</weak_warning> int $i
 * <weak_warning descr="@param just duplicates type hint">@param</weak_warning> <weak_warning descr="Use '?T', not 'T|null'">string|null</weak_warning> $s
 */
function withDuplicatingParam5(int $i, ?string $s) {
}

/**
 * before
 * <weak_warning descr="@param just duplicates type hint">@param</weak_warning> int $i
 * @param string $s
 * this is treated as comment for $s
 */
function withDuplicatingParam6(int $i, string $s) {
}

/**
 * @param ?A $a
 */
function withDuplicatingParam7(A $a) {
}

/**
 * <weak_warning descr="@param can be replaced with type hint 'int'">@param</weak_warning> int $i
 * <weak_warning descr="@param can be replaced with type hint 'string'">@param</weak_warning> string $s
 * @param int[] $i_arr
 * @param int|false $i_f
 */
function withTransformToTypeHints1($i, $s, $i_arr, $i_f) {
}

/**
 * <weak_warning descr="@param can be replaced with type hint 'int'">@param</weak_warning> int $i
 * <weak_warning descr="@param can be replaced with type hint 'string'">@param</weak_warning> string $s
 * @param int[] $i_arr
 * @param int|false $i_f
 */
function withTransformToTypeHints2($i, $s, $i_arr, $i_f) {
}

/**
 * @param int $i comment
 * @param string $s comment
 * @param int[] $i_arr comment
 * @param int|false $i_f comment
 */
function withNoTransformToTypeHints3($i, $s, $i_arr, $i_f) {
}

/**
 * <weak_warning descr="@param can be replaced with type hint 'A'">@param</weak_warning> A $a
 * <weak_warning descr="@param can be replaced with type hint '?A'">@param</weak_warning> ?A $a_null
 * <weak_warning descr="@param can be replaced with type hint 'MsgBuilder'">@param</weak_warning> MsgBuilder $builder
 * <weak_warning descr="@param can be replaced with type hint '?MsgBuilder'">@param</weak_warning> ?\VK\API\Builders\MsgBuilder $builder_full
 * <weak_warning descr="@param can be replaced with type hint '\VK\API\Builders\AnotherBuilder'">@param</weak_warning> \VK\API\Builders\AnotherBuilder $builder_another
 * <weak_warning descr="@param can be replaced with type hint '?string'">@param</weak_warning> <weak_warning descr="Use '?T', not 'T|null'">string|null</weak_warning> $s_null
 * @param <weak_warning descr="Use '?T', not 'T|null'">int[]|null</weak_warning> $i_arr_null
 * @param int|string $complex
 * @param int|false $i_f
 */
function withTransformToTypeHints4($a, $a_null, $builder, $builder_full, $builder_another, $s_null, $i_arr_null, $complex, $i_f) {
}

/**
 * comment
 * <weak_warning descr="@param can be replaced with type hint '?string'">@param</weak_warning> <weak_warning descr="Use '?T', not 'T|null'">string|null</weak_warning> $s_null
 * @param MsgBuilder $builder comment
 */
function withTransformToTypeHints5(int $i, $s_null, $builder) {
}

/**
 * <warning descr="@kphp-infer is deprecated">@kphp-infer</warning>
 * <weak_warning descr="@param can be replaced with type hint '?string'">@param</weak_warning> <weak_warning descr="Use '?T', not 'T|null'">string|null</weak_warning> $s_null
 */
function withTransformToTypeHints6(int $i, $s_null) {
}

/**
 * todo this works wrong: suggests to convert future to int, should be corrected massively somewhen
 * <weak_warning descr="@param can be replaced with type hint 'int'">@param</weak_warning> future<?string> $fs
 */
function withTransformToTypeHints7($fs) {
}

/**
 * @param int ...$ints
 */
function withTransformToTypeHints8(...$ints) {
}

function lambdaInside() {
    /**
     * @param int $s
     * <error descr="@param tag for unexisting argument">@param</error> $asdf int comment
     */
    $f1 = function($s, $i) {
    };

    $f2 = /**
     * @param int $s
     * <error descr="@param tag for unexisting argument">@param</error> $asdf int comment
     */
    function($s, $i) {
    };

    /**
     * @param int $s
     * <warning descr="Use @param {type} $asdf, not @param $asdf {type}">@param</warning> $asdf int comment
     */
    function($s, $asdf) {
    };
}
