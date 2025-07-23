<?php

/**
 * @kphp-immutable-class
 */
class <caret>C1 {
    public int $field;
}

public function foo()
{
    $v = new C1();
    $v->field = 1; // mutation outside of a class, that's ok
}
