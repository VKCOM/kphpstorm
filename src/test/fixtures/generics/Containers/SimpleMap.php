<?php

/**
 * @kphp-generic TKey, TValue
 */
class SimpleMap {
  /** @var TValue[] */
  private $data = [];

  /**
   * @param Pair<TKey, TValue> ...$els
   */
  public function __construct(...$els) {
    foreach ($els as $keyValue) {
      $key                           = $keyValue->first();
      $this->data[$key->serialize()] = $keyValue->second();
    }
  }


  /**
   * @param TKey $key
   * @return TValue
   */
  public function get($key) {
    return $this->data[$key->serialize()];
  }

  /**
   * @param TKey   $key
   * @param TValue $value
   */
  public function set($key, $value): void {
    $this->data[$key->serialize()] = $key;
  }
}
