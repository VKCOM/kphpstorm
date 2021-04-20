<?php

class A {
    public int $i;
    /** @var int with comment */
    public $i2 = 0;
    public ?A $a = null;
    /**
     * @see int
     */
    public int $f6;

    static private string $s1 = 'str';
    protected static ?string $s2 = null;

    /** @var string|false */
    public $c1;
    /** @var mixed */
    static public $c2 = 0;
    /** @var int[] */
    public array $c3;
    // todo this works wrong: suggests to convert future to int, should be corrected massively somewhen
    public int $c4;
}

class B {
    var int $i = 0;
    public ?string $s = '';

    public int $mismatch = -1;
}
