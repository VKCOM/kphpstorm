<?php

/**
 * @param any $expr
 */
function expr_type($expr, string $string) {
}

/**
 * @kphp-generic T
 * @param ?T $arg
 * @return T
 */
function not_null($arg) {
  if ($arg == null) {
    exit();
  }
  return $arg;
}

/**
 * @kphp-generic T
 * @param T $arg
 * @return ?T
 */
function nullable_of($arg) {
  if (0) {
    return null;
  }
  return $arg;
}

/**
 * @param mixed $args
 */
function tuple(...$args) {
  return ${"args"};
}
/**
 * @param mixed $args
 */
function shape($args) {
  return ${"args"};
}

/**
 * @kphp-generic T
 * @param T ...$els
 * @return SimpleVector<T>
 */
function listOf(...$els) {
  return new SimpleVector(...$els);
}

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror($arg) {
  return $arg;
}

/**
 * @kphp-generic T1, T2
 * @param T1 $a1
 * @param T2 $a2
 * @return T1|T2
 */
function combine($a1, $a2) {
  if (0) {
    return $a2;
  }
  return $a1;
}

/**
 * @kphp-generic T1, T2
 * @param T1[]             $array
 * @param class-string<T2> $class
 * @return T2[]
 */
function filter_is_instance($array, $class) {
  return array_filter($array, fn($el) => is_a($el, $class));
}

/**
 * @kphp-generic T
 * @param callable(T,T): bool $gt
 * @param T                   ...$arr
 * @return T
 */
function max_by($gt, ...$arr) {
  $max = array_first_value($arr);
  for ($i = 1; $i < count($arr); ++$i) {
    if ($gt($arr[$i], $max)) {
      $max = $arr[$i];
    }
  }
  return $max;
}

/**
 * @kphp-generic T, DstClass
 * @param T                      $obj
 * @param class-string<DstClass> $to_classname
 * @return DstClass
 */
function instance_cast($obj, $to_classname) {
  return $obj;
}

/**
 * @kphp-generic TElem, ToName
 * @param TElem[]              $arr
 * @param class-string<ToName> $to
 * @return ToName[]
 */
function array_cast($arr, $to) {
  $out = [];
  foreach ($arr as $k => $v) {
    $out[$k] = instance_cast($v, $to);
  }
  return $out;
}

/**
 * @kphp-generic T
 * @param T[] $arr
 * @return T
 */
function array_first_value(array $arr) {
  return $arr[0];
}
