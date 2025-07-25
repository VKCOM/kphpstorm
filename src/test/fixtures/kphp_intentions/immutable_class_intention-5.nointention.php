<?php

class <caret>C1 {
    public int $field;

    public function foo() {
        $this->field = 1; // mutate the field
    }
}
