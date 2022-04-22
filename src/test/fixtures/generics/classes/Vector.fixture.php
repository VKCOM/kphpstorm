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


    /**
     * @kphp-generic T1
     * @param callable(T):T1 $fn
     * @return Vector<T1>
     */
    function map($fn) {
        /** @var T1 $new_data */
        $new_data = array_map($fn, $this->data);
        return new Vector(...$new_data);
    }
}

/**
 * @kphp-generic T
 * @param T ...$els
 * @return Vector<T>
 */
function listOf(...$els) {
    return new Vector(...$els);
}
//

//listOf(1, 2, 3)->get(10);
//listOf("1", "2", "3")->get(10);

$vec = listOf(1, 2)->map(function ($el): string {
    return "$el";
});


/** @noinspection GenericUnnecessaryExplicitInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */


//
//$a = mirror(tuple([new GlobalA()], new \Classes\A() ?? new \Classes\C));
//expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");
//

//use Classes\Base;
//
//class SerializableItem implements Serializable {
//    public function serialize() {}
//
//    /**
//     * @param mixed $data
//     */
//    public function unserialize($data) {}
//}
//
///**
// * Not mutable generic collection.
// *
// * @kphp-generic T: Serializable
// */
//class VectorList1 {
//    /** @var T[] */
//    protected $data = [];
//
//    /**
//     * @param T ...$els
//     */
//    public function __construct(...$els) {
//        $this->data = $els;
//    }
//
//    /**
//     * @return T
//     */
//    public function get(int $index) {
//        return $this->data[$index];
//    }
//}
//
///**
// * @kphp-generic T: Serializable
// * @param T $a
// */
//function acceptSerializable($a) {
//    // Сделать тут тип $a == Serializable, чтобы было базовое автодополнение
//}
//
//////$vec = new VectorList1/*<string>*/(1, 2, 3);
//////$vec = new VectorList1/*<GlobalA>*/(1, 2, 3);
////$vec = new VectorList1/*<Base>*/(1, 2, 3);
////$vec = new VectorList1(new Base);
////$vec = new VectorList1/*<SerializableItem>*/(new SerializableItem());
////
////acceptSerializable(new Base);
////acceptSerializable/*<Base>*/(new Base);
////acceptSerializable(new SerializableItem);
//
//class BaseClass {}
//
//class ChildClass extends BaseClass {
//    /**
//     * @kphp-generic T: self
//     * @param T $a
//     */
//    function f($a) {
//
//    }
//}
//
//$a = new ChildClass();
////$a->f(new BaseClass());
//$a->f(new SerializableItem());
//
///**
// * @kphp-generic T1, T2: T1
// * @param T1 $a
// * @param T2 $b
// */
//function g($a, $b) {
//
//}
//
//g/*<SerializableItem, ChildClass>*/();



