<?php

class <caret>C1 {
    public int $field = 1;

    public function foo() {
        $tmp = $this->field; // no field mutation
    }
}
