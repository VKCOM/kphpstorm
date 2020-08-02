<?php

trait Singleton {
    /** @var self */
    private static $instance = null;

    function __construct() {}

    /** @return self */
    public static function instance() {
        if(!self::$instance)
            self::$instance = new self;
        return self::$instance;
    }
}

class ASingle {
    use Singleton;
}

class BSingle {
    use Singleton;
}

trait AnotherTrait {
    /** @var self */
    static public $anotherInstance;
}

class AnotherA {
    use AnotherTrait;
}

class Holder {
    /** @var ASingle */
    public $a;
    /** @var BSingle */
    public $b;

    public ?AnotherA $another_a;
}

function demo() {
    $h = new Holder;
    $h->a = ASingle::instance();
    $h->b = BSingle::instance();
    <error descr="Can't assign 'BSingle|Singleton' to 'ASingle' $a">$h->a = BSingle::instance()</error>;
    <error descr="Can't assign 'ASingle|Singleton' to 'BSingle' $b">$h->b = ASingle::instance()</error>;

    <error descr="Can't assign 'BSingle' to 'ASingle' $a">$h->a = new BSingle</error>;

    $h->another_a = AnotherA::$anotherInstance;
    <error descr="Can't assign 'ASingle|Singleton' to '?AnotherA' $another_a">$h->another_a = ASingle::instance()</error>;
    <error descr="Can't assign 'AnotherA' to 'ASingle' $a">$h->a = AnotherA::$anotherInstance</error>;
}


