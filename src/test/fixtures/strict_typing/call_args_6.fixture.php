<?php

/** @return mixed|null */
function configGet() {
}

/**
  * @param mixed|int[] $ids
  */
function mcLoad($ids) {}

mcLoad(configGet());

//////////////

// no type declaration

function acceptsInt(int $i) {}

function getUserName($user = false) {
    acceptsInt(<error descr="Can't pass 'false' to 'int' $i">$user</error>);
}

getUserName(<error descr="Can't pass 'int[]' to 'false' $user">['id' => 1]</error>);

//////////////

// lambdas

function withLambda() {
    $f1 = function(int $x) {};
    $f1(1);
    $f1(<error descr="Can't pass 'string' to 'int' $x">'asdf'</error>);
    $f1(<error descr="Can't pass 'array' to 'int' $x">[]</error>);
    $f1(<error descr="No value passed for $x">)</error>;

    $f2 = function($x) {};
    $f2(1);
    $f2('asdf');
    $f2([]);
    $f2(<error descr="No value passed for $x">)</error>;

    $unknown(1);
}
