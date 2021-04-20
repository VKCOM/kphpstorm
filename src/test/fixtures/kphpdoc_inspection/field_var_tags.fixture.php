<?php

class A {
    /** <weak_warning descr="@var can be replaced with field hint 'int'">@var</weak_warning> int */
    public $i;
    /** @var int with comment */
    public $i2 = 0;
    /** <weak_warning descr="@var can be replaced with field hint '?A'">@var</weak_warning> ?A */
    public $a = null;
    /**
     * <weak_warning descr="@var can be replaced with field hint 'int'">@var</weak_warning> int
     * @see int
     */
    public $f6;

    /** <weak_warning descr="@var can be replaced with field hint 'string'">@var</weak_warning> string */
    static private $s1 = 'str';
    /** <weak_warning descr="@var can be replaced with field hint '?string'">@var</weak_warning> <weak_warning descr="Use '?T', not 'T|null'">string|null</weak_warning> */
    protected static $s2 = null;

    /** @var string|false */
    public $c1;
    /** @var mixed */
    static public $c2 = 0;
    /** @var int[] */
    public array $c3;
    // todo this works wrong: suggests to convert future to int, should be corrected massively somewhen
    /** <weak_warning descr="@var can be replaced with field hint 'int'">@var</weak_warning> future<int> */
    public $c4;
}

class B {
    /** <weak_warning descr="@var just duplicates type hint">@var</weak_warning> int */
    var int $i = 0;
    /** <weak_warning descr="@var just duplicates type hint">@var</weak_warning> ?string */
    public ?string $s = '';

    /** <error descr="@var mismatches type hint">@var</error> string */
    public int $mismatch = -1;
}
