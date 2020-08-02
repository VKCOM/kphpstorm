<?php

class A {
    /**
     * @param int $i
     * @param string $s
     */
    static function f1($i, $s) {
        self::f2($i, $s);
        self::f2(<error descr="Can't pass 'string' to 'int' $i">$s</error>, <error descr="Can't pass 'int' to 'string' $s">$i</error>);
    }

    static function f2(int $i, string $s) {
        self::f1(<error descr="Can't pass 'A' to 'int' $i">new A</error>, $s);
        self::f1(5, [][0]);

        self::f3([]);
        self::f3([1,2,3]);
        self::f3(<error descr="Can't pass 'string[]' to 'int[]' $a">['1','2','3',$s]</error>);
        self::f3([$i, $s]);     // mixed array, can't detect
        self::f3([1,2,3,null]);
        self::f3(<error descr="Can't pass 'null' to 'int[]' $a">null</error>);

        self::f4([]);
        self::f4([1,2,3]);
        self::f4(<error descr="Can't pass 'string[]' to '?int[]' $a">['1','2','3',$s]</error>);
        self::f4([$i, $s]);     // mixed array, can't detect
        self::f4([1,2,3,null]);
        self::f4(null);
    }

    /** @param int[] $a */
    static function f3(array $a) {
    }

    /** @param ?int[] $a */
    static function f4(?array $a) {
    }
}
