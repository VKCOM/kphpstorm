<?php

namespace asdf;

/**
 * <error descr="@return mismatches type hint">@return</error> int
 */
function withReturnConflict1() : void {}

/**
 * <error descr="@return mismatches type hint">@return</error> int|false
 */
function withReturnConflict2() : int { return 0; }

/**
 * <error descr="@return mismatches type hint">@return</error> ?int[]
 */
function withReturnConflict3() : array { return []; }

/**
 * <error descr="@return mismatches type hint">@return</error> comment
 */
function withReturnConflict4() : ?string { return ''; }

/**
 * <weak_warning descr="@return just duplicates type hint">@return</weak_warning> int
 */
function withDuplicating1() : int { return 0; }

/**
 * comment
 * <weak_warning descr="@return void can be deleted">@return</weak_warning> void
 */
function withDuplicating2() : void {}

/**
 * <weak_warning descr="@return void can be deleted">@return</weak_warning> void
 */
function withReturnVoid() {}

/**
 * <weak_warning descr="@return just duplicates type hint">@return</weak_warning> ?int
 */
function withDuplicating3() : ?int { return 0; }

/**
 * <weak_warning descr="@return just duplicates type hint">@return</weak_warning> classname
 */
function withDuplicating4() : classname { return new classname; }

/**
 * <weak_warning descr="@return just duplicates type hint">@return</weak_warning> ?\asdf\full
 */
function withDuplicating5() : ?full { return null; }

// below after replacement {body} becomes on the next line, but in real IDE in depends on formatting settings

/**
 * <weak_warning descr="@return can be replaced with type hint 'int'">@return</weak_warning> int
 */
function withReplacement1() { return 0; }

/**
 * <weak_warning descr="@return can be replaced with type hint 'int'">@return</weak_warning> int
 */
function withReplacement2() { return 0; }

/**
 * Implicit kphp-infer
 * <weak_warning descr="@return can be replaced with type hint '?string'">@return</weak_warning> ?string
 */
function withReplacement3(int $i) { return null; }

/**
 * This tag is not deleted, as type hint is not scalar
 * <weak_warning descr="@return can be replaced with type hint 'another'">@return</weak_warning> \asdf\another
 */
function withReplacement4() { return new \asdf\another; }

/**
 * @return string|string[]
 */
function withNoReplacement5() { return 1 ? '' : ['']; }

/**
 * <weak_warning descr="@return can be replaced with type hint '?bool'">@return</weak_warning> <weak_warning descr="Use '?T', not 'T|null'">boolean|null</weak_warning>
 */
function withReplacement6(int $i) { return true; }

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
 * @return <weak_warning descr="Use '?T', not 'T|null'">classname[]|null</weak_warning>
 */
function withNoAdding3() { return null; }

/**
 * @return ?classname[]
 */
function withNoAdding5() { return [new classname]; }


trait Mix {
  /**
   * <error descr="@param mismatches type hint">@param</error> $s self
   */
  function accept(int $s) {
  }

  /**
   * <weak_warning descr="@return just duplicates type hint">@return</weak_warning> Mix
   */
  function get1(): self {
    return new self;
  }
}
