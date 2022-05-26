<?php

/**
 * @kphp-generic T
 */
class Vector {
  /** @var T[] */
  private $data = [];

  /**
   * @param T ...$els
   */
  public function __construct(...$els) {
    $this->data = $els;
  }

  /**
   * @return T
   */
  public function get(int $index) {
    return $this->data[$index];
  }

  /**
   * @param T $data
   */
  public function add($data): void {
    $this->data[] = $data;
  }

  /**
   * @return T[]
   */
  public function raw(): array {
    return $this->data;
  }

  /**
   * @kphp-generic T1
   * @param callable(T):T1 $fn
   * @return SimpleVector<T1>
   */
  function map($fn) {
    /** @var T1 $new_data */
    $new_data = array_map($fn, $this->data);
    return new SimpleVector(...$new_data);
  }

  /**
   * @param callable(T):bool $fn
   * @return SimpleVector<T>
   */
  function filter($fn) {
    /** @var T $new_data */
    $new_data = array_filter($this->data, $fn);
    return new SimpleVector(...$new_data);
  }

  /**
   * @param callable(T): void $fn
   */
  function foreach($fn) {
    foreach ($this->data as $el) {
      $fn($el);
    }
  }

  /**
   * @param callable(string, T): void $fn
   */
  function foreach_key_value($fn) {
    foreach ($this->data as $key => $el) {
      $fn($key, $el);
    }
  }

  /**
   * @kphp-generic T1
   * @param class-string<T1> $class
   * @return SimpleVector<T1>
   */
  function filter_is_instance($class) {
    // TODO: replace Foo with T1
    return $this->filter/*<Foo>*/ (fn($el) => is_a($el, $class));
  }

  /**
   * @kphp-generic T1
   * @param SimpleVector<T1> $other
   * @return SimpleVector<T|T1>
   */
  function combine_with($other) {
    // TODO: replace Foo with T|T1
    return new SimpleVector/*<T>*/ (...array_merge($this->data, $other->raw()));
  }
}
