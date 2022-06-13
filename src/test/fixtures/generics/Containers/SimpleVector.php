<?php

/**
 * @kphp-generic T
 */
class SimpleVector {
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
}
