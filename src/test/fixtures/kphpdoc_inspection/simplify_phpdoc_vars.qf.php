<?php

class A {
    /** @var int|false */
    public $f1;
    /** @var int|string|false */
    private $f2 = false;

    /** @var ?string some comment */
    protected $f3 = [
        self::CONST,
    ];

    /**
     * some comment
     * @var int[]
     */
    public $f4;
    /** some comment
     *  @var int[]
     */
    public $f5;
    /**
     * @var int[]
     * @see int
     */
    public $f6;
    /**
     * @var int[]
     * some comment
     */
    public $f7;
}

function withDocsInside() {
    /** @var int $s1 */
    /** @var int $s2 comment */
    /**
     * any
     * @var int $s3
     */
    /**
     * @var string $s4
     * @var string $s5
     */
    /** @var asdfasdfasdf|false */
    $m = null;
}
