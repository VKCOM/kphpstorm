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
     * @kphp-generic T1
     * @param class-string<T1> $class
     * @return T1[]
     */
    function filter_is_instance($class) {
        return array_filter($this->data, fn($el) => is_a($el, $class));
    }

    /**
     * @kphp-generic T1
     * @param Vector<T1> $other
     * @return Vector<T|T1>
     */
    function combine_with($other) {
        return new Vector/*<T|T1>*/(array_merge($this->data, $other->data));
    }
}
