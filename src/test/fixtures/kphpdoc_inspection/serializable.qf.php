<?php

/**
 * @kphp-serializable
 * @kphp-reserved-fields [3]
 */
class A {
    /** @kphp-serialized-field 1 */
    public int $f1;
    /** @kphp-serialized-field none */
    public ?A $none;
    /**
     * @var int
     * @kphp-serialized-field 2
     */
    public $f2;
    /**
     * @kphp-serialized-field 4
     */
    public $f4;
    /**
     * @kphp-serialized-field 5
     */
    public int $f5;
    /**
     * comment 1
     * @kphp-serialized-field 6
     * @var int comment 2
     */
    public $f6;
    /**
     * @kphp-serialized-field 7
     * @var int comment
     */
    public $f7;
    /** asdf
     * @kphp-serialized-field 8
     * @var int comment
     */
    public $f8;
    /**
     * asdf @var int comment
     * @kphp-serialized-field 9
     */
    public $f9;
    /**
     * asdf {@var int comment} comment2
     * @kphp-serialized-field 10
     */
    protected int $f10;
}

/**
 * @kphp-serializable
 * @kphp-reserved-fields [3]
 */
class A2 {
    /** @kphp-serialized-field 1 */
    public $conflict1;
    /** @kphp-serialized-field 1 */
    public $conflict2;
    /** @kphp-serialized-field 2 */
    public $ok;
    /** @kphp-serialized-field 3 */
    public $conflict_with_reserved;
}

/**
 * @kphp-serializable
 */
class A3 {
    /** @kphp-serialized-field 1 */
    public $conflict1;
    /** @kphp-serialized-field 1 */
    public $conflict2;
    /** @kphp-serialized-field 2 */
    public $ok;
    /** @kphp-serialized-field 3 with comment */
    public $ok2;
    /** @kphp-serialized-field none */
    public $ok3;
    /** @kphp-serialized-field none with comment */
    private $ok4;

    static int $static_field;
}

class A4 {
    public $not_applicable;
    /**
     * asdf
     * @var int
     */
    public int $not_applicable_2;
}
