<?php

/**
 * @var ffi_cdata<example, union Foo> $a
 * @var ffi_cdata<example, struct Foo> $b
 * @var ffi_cdata<C, void*> $c
 * @var ffi_cdata<C, const int32_t*> $d
 * @var ffi_cdata<vector, struct Vector2f**> $e

 * @var ffi_scope<vector> $f
 * @var ffi_scope<h3> $g
 */
function f1($a, $b, $c, $d, $e, $f, $g) {
  expr_type($a, "any");
  expr_type($b, "any");
  expr_type($c, "any");
  expr_type($d, "any");
  expr_type($e, "any");
  expr_type($f, "any");
  expr_type($g, "any");
}
