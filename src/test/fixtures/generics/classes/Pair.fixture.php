<?php

/**
 * @kphp-generic T1, T2
 */
class Pair {
    /** @var T1 */
    private $first;

    /** @var T2 */
    private $second;

    /**
     * @param T1 $first
     * @param T2 $second
     */
    public function __construct($first, $second) {
        $this->first = $first;
        $this->second = $second;
    }

    /**
     * @return T1
     */
    public function first() {
        return $this->first;
    }

    /**
     * @return T2
     */
    public function second() {
        return $this->second;
    }
}
