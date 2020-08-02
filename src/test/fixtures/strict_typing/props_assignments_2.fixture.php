<?php

class A {
    public $i = 0;
    public string $s;
    /** @var int[] */
    public array $i_arr;
    /** @var string[] */
    public $s_arr;
    /** @var (string|null)[] */
    public array $s_null_arr;
    /** @var ?int */
    public $i_null;
    /** @var ?string */
    public $s_null;
    /** @var ?A */
    public $a_null;
    /** @var A|null */
    public $a_null_2;
    /** @var A */
    public $a;
    /** @var A[] */
    public array $a_arr;
    public $b = false;
    public float $f = 0;
    /** @var ?float */
    public $f_null;
    /** @var array */
    public $just_arr;
    /** @var array[] */
    public array $arr_of_just_arr;
    /** @var ?int[] */
    public $i_arr_null;
    /** @var (?int)[] */
    public $i_null_arr;
}

class B {
    /** @var mixed */
    public $m;
    /** @var mixed|null */
    public $m_null;
    public $s = '';
    /** @var int|false */
    public $i_false = false;
}

function getB() : B {
    return new B;
}

/** @return string[] */
function forceArrayString(array $arr) { return $arr; }

function not_null($any_value) {
    return $any_value;
}

function not_false($any_value) {
    return $any_value;
}

function demo() {
    $a = new A;
    $b = getB();

    $a->i = 2 * 3;          // PhpStorm infers 'float' at rhs, that's why int should accept float
    $a->i = (int)$a->s;
    <error descr="Can't assign 'int' to 'string' $s">$a->s = $a->i</error>;
    <error descr="Can't assign 'int[]' to 'string[]' $s_arr">$a->s_arr = $a->i_arr</error>;
    $a->s_arr = forceArrayString($a->i_arr);
    $a->a_null = null;
    $a->a_null = new A;
    $a->a_null_2 = null;
    $a->a_null_2 = new A;
    $a->a = null;
    $a->a = new A;
    $a->a_arr = [new A];
    $a->i_arr_null = $a->i_arr;
    $a->i_null_arr = $a->i_arr;
    <error descr="Can't assign 'int|float' to 'string' $s">$a->s = $a->f</error>;
    <error descr="Can't assign 'bool' to 'string' $s">$a->s = $a->b</error>;
    $bools = [$a->b, $a->b, $a->b];
    <error descr="Can't assign 'bool' to 'int|float' $f">$a->f = $bools[0]</error>;
    <error descr="Can't assign 'string[]' to 'A[]' $a_arr">$a->a_arr = ['s', 's']</error>;
    $a->a_arr = [1, null];      // can't detect, just "array"
    $a->a_arr = [new A, new B];
    $a->i_arr = [new A, new B, 's'];
    /** @var unknown $u */
    <error descr="Can't assign 'unknown' to '?A' $a_null_2">$a->a_null_2 = $u</error>;
    <error descr="Can't assign '?int' to '?string' $s_null">$a->s_null = 1 ? null : 4</error>;
    <error descr="Can't assign '?int[]' to 'int[]' $i_arr">$a->i_arr = $a->i_arr_null</error>;

    $undetectable = [new A, new B];
    $a->i_null = $undetectable[0];
    $a->a = $undetectable[0];
    $a->a_arr = $undetectable[100500];

    <error descr="Can't assign 'var' to '?A' $a_null">$a->a_null = $b->m</error>;
    <error descr="Can't assign 'var' to '?A' $a_null">$a->a_null = $b->m_null</error>;
    $a->s_null = $b->s;
    $b->m = $a->s;
    $b->m_null = $a->s;
    $b->m[] = $a->s;
    $b->m_null[] = $a->s;
    $b->m = $a->s_null;
    $b->m[] = $a->s_null;
    $b->m = $a->i;
    $b->m[] = $a->i;
    $b->m = $a->i_arr;
    $b->m[] = $a->i_arr;
    $b->m = $a->f_null;
    $b->m[] = $a->f_null;
    $b->m = $a->b;
    $b->m[] = $a->b;
    $b->m = $a->i_arr_null;
    $b->m[] = $a->i_arr_null;
    $b->m = $a->i_null_arr;
    $b->m[] = $a->i_null_arr;
    $b->m = forceArrayString([]);
    $b->m = $a->i_arr;
    $b->m = $a->arr_of_just_arr;
    $b->m = $a->just_arr;
    $b->m = $a->s_arr;
    $b->m = $a->s_null_arr;
    $b->m_null = $a->s_null_arr;

    $b = new B;
    $b->m = $b->m_null;

    $a = new A;
    $b = new B;

    $a->arr_of_just_arr = $a->just_arr;
    $a->just_arr = $a->arr_of_just_arr;

    $a = new A;
    $b = new B;

    $a->i_arr = $a->just_arr;
    $a->i_arr = [1,2,(int)'3'];
    $a->i_arr[0] = 2;
    $a->i_arr[0] = 3 * 9;
    $a->s_null_arr = $a->just_arr;
    $a->a_arr = $a->just_arr;
    <error descr="Can't assign 'var' to 'array' $just_arr">$a->just_arr = $b->m</error>;
    $a->just_arr = $a->i_arr;
    $a->just_arr = $bools;
    $a->i_arr_null[] = 3;
    $a->i_null_arr[] = 3;
    $a->i_null_arr[] = null;
    $a->i_null_arr[] = $a->i_null;
    <error descr="Can't assign 'null' to 'int' $i_arr_null[*]">$a->i_arr_null[] = null</error>;
    <error descr="Can't assign '?int' to 'int' $i_arr_null[*]">$a->i_arr_null[] = $a->i_null</error>;
    <error descr="Can't assign 'array[]' to 'int[]' $i_arr">$a->i_arr = $a->arr_of_just_arr</error>;
    <error descr="Can't assign 'array[]' to 'A[]' $a_arr">$a->a_arr = $a->arr_of_just_arr</error>;

    // important! if we use /** @var string|null $s_null */ smartcasts won't work */
    $s_null = 1 ? 'str' : null;

    $a->s_null = $s_null;
    <error descr="Can't assign '?string' to 'string' $s">$a->s = $s_null</error>;
    <error descr="Can't assign '?string' to '?int' $i_null">$a->i_null = $s_null</error>;
    <error descr="Can't assign '?string' to 'string' $s_arr[*]">$a->s_arr[] = $s_null</error>;
    $a->s = (string)$s_null;
    $b->m = $s_null;
    <error descr="Can't assign 'null[]|string[]' to 'string[]' $s_arr">$a->s_arr = [$s_null]</error>;
    <error descr="Can't assign '?string' to 'string' $s_arr[*]">$a->s_arr[] = $s_null</error>;
    <error descr="Can't assign '?string' to 'int' $i_arr[*]">$a->i_arr[0] = $s_null</error>;
    <error descr="Can't assign 'B' to 'int' $i_arr[*]">$a->i_arr[0] = new B</error>;
    <error descr="Can't assign 'B' to 'A' $a_arr[*]">$a->a_arr['s'.'b'] = new B</error>;
    <error descr="Can't assign 'B' to 'null' $i[*]">$a->i[] = new B</error>;
    $a->a_arr['s'.'b'] = new A;

    // smart cast
    if ($s_null !== null) {
        $a->s = $s_null;
        $a->s_arr = [$s_null];
        $a->s_arr[] = $s_null;
        $b->s = $s_null;
    }
    if ($s_null) {
        $a->s = $s_null;
        $a->s_arr = [$s_null];
        $a->s_arr[] = $s_null;
        $b->s = $s_null;
    }
    if (is_string($s_null)) {
        $a->s = $s_null;
        $a->s_arr = [$s_null];
        $a->s_arr[] = $s_null;
        $b->s = $s_null;
    }
    // smart cast doesn't work for ->props
    if ($a->s_null !== null) {
        <error descr="Can't assign '?string' to 'string' $s">$a->s = $a->s_null</error>;
    }

    // since 2020.2, 'false' is a separate type, earlier it was inferred as 'bool'
    $s_false = 1 ? 'str' : false;
    <error descr="Can't assign 'string|false' to 'string' $s">$a->s = $s_false</error>;       // ok, 'false'/'bool' assignment is not strict
    <error descr="Can't assign 'string|false' to '?string' $s_null">$a->s_null = $s_false</error>;
    <error descr="Can't assign 'string|false' to '?int' $i_null">$a->i_null = $s_false</error>;
    $b->m = $s_false;
    <error descr="Can't assign 'false[]|string[]' to 'string[]' $s_arr">$a->s_arr = [$s_false]</error>;
    <error descr="Can't assign 'string|false' to 'int' $i_arr[*]">$a->i_arr[] = $s_false</error>;
    // smart casts don't work for |false
    if ($s_false !== false) {
        $a->s = $s_false;
        $a->s_arr[] = $s_false;
    }

}

function demo2() {
    $a = new A;
    $b = new B;
    <error descr="Can't assign 'var' to 'bool' $b">$a->b = $b->m</error>;
    <error descr="Can't assign 'var' to '?float' $f_null">$a->f_null = $b->m</error>;
    <error descr="Can't assign 'var' to 'A' $a">$a->a = $b->m</error>;
    <error descr="Can't assign 'var' to '?A' $a_null">$a->a_null = $b->m</error>;
    <error descr="Can't assign 'var' to '?A' $a_null_2">$a->a_null_2 = $b->m</error>;
    <error descr="Can't assign 'var' to 'A[]' $a_arr">$a->a_arr = $b->m</error>;
    <error descr="Can't assign 'var' to 'int[]' $i_arr">$a->i_arr = $b->m</error>;
    <error descr="Can't assign 'var' to 'string[]' $s_arr">$a->s_arr = $b->m</error>;
    <error descr="Can't assign 'var' to 'int[]' $i_arr">$a->i_arr = $b->m</error>;

    $a = new A;
    <error descr="Can't assign 'A' to 'var' $m">$b->m = $a->a</error>;
    <error descr="Can't assign 'A[]' to 'var' $m">$b->m = $a->a_arr</error>;

    $b = new B;
    <error descr="Can't assign 'var' to 'null[]|string[]' $s_null_arr">$a->s_null_arr = $b->m</error>;
    <error descr="Can't assign 'var' to 'array[]' $arr_of_just_arr">$a->arr_of_just_arr = $b->m</error>;
}

function demo3(string $s) {
    $a = new A;
    $a->s = $s;
}

function demo4(string $s = null) {
    $a = new A;
    <error descr="Can't assign '?string' to 'string' $s">$a->s = $s</error>;
}

function demo5(?string $s) {
    $a = new A;
    <error descr="Can't assign '?string' to 'string' $s">$a->s = $s</error>;
}

function demo6() {
    $a = new A;
    $b = new B;
    $a->i = not_null($a->i_null);
    <error descr="Can't assign '?int' to 'int' $i">$a->i = not_false($a->i_null)</error>;
    <error descr="Can't assign '?int[]' to 'int[]' $i_arr">$a->i_arr = $a->i_arr_null</error>;
    $a->i_arr = not_null($a->i_arr_null);
    $a->i = not_false($b->i_false);
}

class More {
    /** @var string */
    public $s;
    /** @var string|null */
    public $s_null;

    function set(string $s = null) {
        $a = new A;
        <error descr="Can't assign '?string' to 'string' $s">$a->s = $s</error>;
        <error descr="Can't assign '?string' to 'string' $s">$this->s = $s</error>;
        $this->s_null = $s;
    }
}


class DemoDay {
  /** @var mixed */
  var $mixed;
  /** @var mixed[] */
  var $arr;
  /** @var self[] */
  var $arr_of_instances;
  /** @var int[] */
  var $arr_of_ints;

  function home1() {
    <error descr="Can't assign 'var' to 'var[]' $arr">$this->arr = $this->mixed</error>;
    $this->arr = (array)$this->mixed;
  }

  function home2() {
    $this->mixed = $this->arr;
  }

  function home3() {
    <error descr="Can't assign 'var' to 'DemoDay[]' $arr_of_instances">$this->arr_of_instances = $this->mixed</error>;
  }

  function home4() {
    <error descr="Can't assign 'DemoDay[]' to 'var' $mixed">$this->mixed = $this->arr_of_instances</error>;
  }

  function home5() {
    <error descr="Can't assign 'var' to 'int[]' $arr_of_ints">$this->arr_of_ints = $this->mixed</error>;
  }

  function home6() {
    $this->mixed = $this->arr_of_ints;
  }
}

/** @return force(string) */
function returnTipaString() {
    return 5;
    // as a result, force(string)|int -> assume string in assignments
}

function demoTipaString() {
    $a = new A;
    $a->s = returnTipaString();
    <error descr="Can't assign 'string' to 'int' $i">$a->i = returnTipaString()</error>;
}

