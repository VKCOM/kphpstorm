<?php
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */

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

"Неизменяемый список"; {
    $vec = new VectorList(1, 2, 3);
    $vec->get(100);
    $vec->add(200);
}

"Изменяемый список"; {
    $mut = new MutableList(1, 2, 3);
    $mut->add(100);
    $mut->remove(1);
    $mut->get(100);
}

