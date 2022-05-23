<?php

/**
 * Not mutable generic collection.
 *
 * @kphp-generic T
 */
class VectorList {
  /** @var T[] */
  protected $data = [];

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
}
