<?php

class A {
    <weak_warning descr="Convert @var to single line">/**
     * @var int|false
     */</weak_warning>
    public $f1;
    <weak_warning descr="Convert @var to single line">/**
     * @var int|string|false
     */</weak_warning>
    private $f2 = false;
    
    <weak_warning descr="Convert @var to single line">/**
     * @var ?string some comment
     */</weak_warning>
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
    <weak_warning descr="Convert @var to single line">/**
     * @var int $s2 comment
     */</weak_warning>
    /**
     * any
     * @var int $s3
     */
    /**
     * @var string $s4
     * @var string $s5
     */
    <weak_warning descr="Convert @var to single line">/**
     * @var asdfasdfasdf|false
     */</weak_warning>
    $m = null;
}
