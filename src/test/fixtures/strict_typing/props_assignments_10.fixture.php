<?php
// warning! this test is known to be failing when running tests,
// though it seems okay in real IDE
// the reason is using str_replace and other standard functions: it seems, that tests don't have necessary php stubs
// moreover, different versions of IDEs behave differently


class A {
    public string $s;
    /** @var string[] */
    public $s_arr;
    /** @var int[] */
    public array $i_arr;
    /** @var A */
    public $a;
    /** @var mixed */
    public $var;
}

// while running tests, standard library is not included
// so, write prototypes of functions we are testing
if(0) {
    /** @return string|null */
    function bcdiv(string $s1, string $s2) {}
    /** @return string|null */
    function bcmod(string $s1, string $s2) {}
    /** @return unknown */
    function unserialize(string $str) {}
    /** @return any */
    function json_decode(string $str) {}
    /** @return string|string[] */
    function str_replace($search, $replace, $subject) {}
    /** @return string|string[] */
    function substr_replace($string, $replacement, $start, $length = 0) {}

    function array_filter(array $a, ?callable $c = null) { return ${'a'}; }
    function array_values(array $a) { return ${'a'}; }

    function array_first_value(array &$a) { $r = current($a); return ${'r'}; }
    function array_last_value(array &$a) { $r = current(${'a'}); return ${'a'}; }
    function array_filter_by_key(array &$a, callable $callback) { return ${'a'}; }
}

function getS() { return 's'; }
function getSArr() { return ['s']; }
/** @return int|bool */
function getTrash() { }

class S {
    /** @return A */
    function unserialize($str) {}
}

function demo() {
    $a = new A;
    // bcdiv() in PHP (and in PhpStorm) returns string|null, but in KPHP it returns string and this compiles
    $a->s = bcdiv('1', '2');
    // to make this be compiled, force(string) in pipe works
    // @see ForcingTypeProvider

    $a->s = 1 ? bcmod('1', '2') : '0';

    $some1 = unserialize('');
    $a->var = $some1;
    <error descr="Can't assign 'mixed' to 'A' $a">$a->a = $some1</error>;
    $a->a = S::unserialize('');

    $some2 = json_decode('');
    $a->var = $some2;
    <error descr="Can't assign 'mixed' to 'A' $a">$a->a = $some2</error>;

    $a->s = substr_replace('s', 'r', 0, 1);

    $s = str_replace('search', 'replace', 'subject');
    $a->s = $s;
    // whyever these lines don't work in fixture tests, but work in real IDE
//     $a->s = str_replace('search', 'replace', $s);
//     $a->s = str_replace('search', 'replace', substr_replace('s', 'r', 0, 1));
//     $a->s = str_replace('search', 'replace', str_replace('search', 'replace', substr_replace('s', 'r', 0, 1)));
    $a->s = $s[0];
    $a->s = str_replace('search', 'replace', getS());
    $a->s = str_replace('search', 'replace', getSArr()[1]);

    $s_arr = str_replace('search', 'replace', ['subject1']);
    <error descr="Can't assign 'string[]' to 'string' $s">$a->s = $s_arr</error>;
    $a->s_arr = $s_arr;
    $a->s_arr = str_replace('search', 'replace', [getS()]);
    $a->s_arr[] = $s_arr[0];
    $a->s_arr = str_replace('search', 'replace', getSArr());

    <error descr="Can't assign 'int|bool' to 'string' $s">$a->s = str_replace('s', 'r', getTrash())</error>;
}

function demo2() {
    $a = new A;
    $a->s = array_first_value($a->s_arr);
    <error descr="Can't assign 'string' to 'A' $a">$a->a = array_first_value($a->s_arr)</error>;
    $a->s_arr[] = array_last_value($a->s_arr);
    <error descr="Can't assign 'mixed' to 'string' $s">$a->s = array_first_value($a->var)</error>;
    <error descr="Can't assign 'mixed' to 'A' $a">$a->a = array_first_value($a->var)</error>;
    $a->var = array_first_value($a->var);
    $a->s_arr = array_filter_by_key($a->s_arr, function($k) { return true; });
    <error descr="Can't assign 'int[]' to 'string[]' $s_arr">$a->s_arr = array_filter_by_key($a->i_arr, function($k) { return true; })</error>;
}

function demo3(array $argv) {
    $a = new A;
    $a->s = str_replace('', '', $argv[0]);
    $a->s = str_replace('', '', (string)$argv[0]);
}


class AA {
    /** @var AA[] */
    public array $aaArr;
    /** @var AA */
    public array $aa;
}

/** @return AA[] */
function getArr() { return []; }

function demo4() {
  $filtered = array_filter(getArr());
  $values = array_values($filtered);
  $aa = new AA;
  <error descr="Can't assign 'AA[]' to 'AA' $aa">$aa->aa = $values</error>;
  $aa->aaArr = $values;
}
