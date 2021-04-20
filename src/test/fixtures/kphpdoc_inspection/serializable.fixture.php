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
     * <weak_warning descr="@var can be replaced with field hint 'int'">@var</weak_warning> int
     * @kphp-serialized-field 2
     */
    public $f2;
    public <error descr="Field has no @kphp-serialized-field tag">$f4</error>;
    public int <error descr="Field has no @kphp-serialized-field tag">$f5</error>;
    /**
     * comment 1
     * @var int comment 2
     */
    public <error descr="Field has no @kphp-serialized-field tag">$f6</error>;
    /** @var int comment */
    public <error descr="Field has no @kphp-serialized-field tag">$f7</error>;
    /** asdf @var int comment */
    public <error descr="Field has no @kphp-serialized-field tag">$f8</error>;
    /**
     * asdf @var int comment
     */
    public <error descr="Field has no @kphp-serialized-field tag">$f9</error>;
    /**
     * asdf {@var int comment} comment2
     */
    protected int <error descr="Field has no @kphp-serialized-field tag">$f10</error>;
}

/**
 * @kphp-serializable
 * <error descr="Index 3 is used in field $conflict_with_reserved">@kphp-reserved-fields [3]</error>
 */
class A2 {
    /** <error descr="Duplicate index with field $conflict2">@kphp-serialized-field 1</error> */
    public $conflict1;
    /** <error descr="Duplicate index with field $conflict1">@kphp-serialized-field 1</error> */
    public $conflict2;
    /** @kphp-serialized-field 2 */
    public $ok;
    /** <error descr="This index is listed in @kphp-reserved-fields">@kphp-serialized-field 3</error> */
    public $conflict_with_reserved;
}

/**
 * @kphp-serializable
 */
class A3 {
    /** <error descr="Duplicate index with field $conflict2">@kphp-serialized-field 1</error> */
    public $conflict1;
    /** <error descr="Duplicate index with field $conflict1">@kphp-serialized-field 1</error> */
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

/**
 * <warning descr="Tag is not applicable here">@kphp-reserved-fields</warning> [4]
 */
class A4 {
    /** <warning descr="Tag is not applicable here">@kphp-serialized-field</warning> 1 */
    public $not_applicable;
    /**
     * asdf
     * <warning descr="Tag is not applicable here">@kphp-serialized-field</warning> 2
     * <weak_warning descr="@var just duplicates type hint">@var</weak_warning> int
     */
    public int $not_applicable_2;
}
