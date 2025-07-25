<?php

class <caret>C1 {
    public int $field;

    public function __construct(int $arg)
    {
        $this->field = $arg; // field mutation in constructor, that's ok
    }
}
