<?php
/** @noinspection GenericUnnecessaryExplicitInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */


//
//$a = mirror(tuple([new GlobalA()], new \Classes\A() ?? new \Classes\C));
//expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");
//

use Classes\Base;

class SerializableItem implements Serializable {
    public function serialize() {}

    /**
     * @param mixed $data
     */
    public function unserialize($data) {}
}

/**
 * Not mutable generic collection.
 *
 * @kphp-generic T: Serializable
 */
class VectorList1 {
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
 * @kphp-generic T: Serializable
 * @param T $a
 */
function acceptSerializable($a) {
    // Сделать тут тип $a == Serializable, чтобы было базовое автодополнение
}

//$vec = new VectorList1/*<string>*/(1, 2, 3);
//$vec = new VectorList1/*<GlobalA>*/(1, 2, 3);
$vec = new VectorList1/*<Base>*/(1, 2, 3);
$vec = new VectorList1(new Base);
$vec = new VectorList1/*<SerializableItem>*/(new SerializableItem());

acceptSerializable(new Base);
acceptSerializable/*<Base>*/(new Base);
acceptSerializable(new SerializableItem);
