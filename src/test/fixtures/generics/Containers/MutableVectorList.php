<?php

/**
 * Mutable generic collection.
 *
 * @kphp-generic T
 */
class MutableList extends VectorList {
  /**
   * @param T $data
   */
  public function add($data): void {
    $this->data[] = $data;
  }

  public function remove(int $index): void {
    unset($this->data[$index]);
  }
}
