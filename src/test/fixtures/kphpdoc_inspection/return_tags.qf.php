<?php

namespace asdf;

function withReturnConflict1() : void {}

function withReturnConflict2() : int { return 0; }

function withReturnConflict3() : array { return []; }

function withReturnConflict4() : ?string { return ''; }

function withDuplicating1() : int { return 0; }

/**
 * comment
 */
function withDuplicating2() : void {}

function withReturnVoid() {}

function withDuplicating3() : ?int { return 0; }

function withDuplicating4() : classname { return new classname; }

function withDuplicating5() : ?full { return null; }

// below after replacement {body} becomes on the next line, but in real IDE in depends on formatting settings

function withReplacement1(): int
{ return 0; }

function withReplacement2(): int
{ return 0; }

/**
 * Implicit kphp-infer
 */
function withReplacement3(int $i): ?string
{ return null; }

/**
 * This tag is not deleted, as type hint is not scalar
 */
function withReplacement4(): another
{ return new \asdf\another; }

/**
 * @return string|string[]
 */
function withNoReplacement5() { return 1 ? '' : ['']; }

function withReplacement6(int $i): ?bool
{ return true; }

/**
 * @return int comment
 */
function withNoAdding1() { return 0; }

/**
 * @return int comment
 */
function withNoAdding1v2(): int { return 0; }

/**
 * @return int[]
 */
function withNoAdding2() { return [1,2,3]; }

/**
 * @return ?classname[]
 */
function withNoAdding3() { return null; }

/**
 * @return ?classname[]
 */
function withNoAdding5() { return [new classname]; }


trait Mix {
    function accept(int $s) {
  }

    function get1(): self {
    return new self;
  }
}
