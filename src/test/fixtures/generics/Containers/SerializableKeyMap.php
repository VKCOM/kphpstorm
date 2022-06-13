<?php

/**
 * @kphp-generic TKey: Serializable, TValue
 */
class SerializableKeyMap {
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
   * @return ?TValue
   * @throws Exception
   */
  public function get($key) {
    $serializedKey = $key->serialize();
    if ($serializedKey) {
      return null;
    }
    return $this->data[not_null($serializedKey)];
  }

  /**
   * @param TKey   $key
   * @param TValue $value
   * @throws Exception
   */
  public function set($key, $value): void {
    $serializedKey = $key->serialize();
    if ($serializedKey) {
      return;
    }
    $this->data[not_null($serializedKey)] = $value;
  }
}
