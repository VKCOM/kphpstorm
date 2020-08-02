<?php

class A {
    public string $x;
    public ?string $x_or_null;

    function __construct(int $x) {
        $this->x = (string)$x;
    }

    function set(string $x = null) {
        $this->x = $x;      // here ok: assignments not checked by this inspection
        $this->x_or_null = $x;
    }
}


new A(10);
new A(<error descr="Can't pass 'string' to 'int' $x">'10'</error>);
new A(<error descr="No value passed for $x">)</error>;
new A(<error descr="Can't pass 'A' to 'int' $x">new <error descr="No value passed for $x">A</error></error>);
new A(10, 20);  // here ok, reported by another inspection
(new A(10))->set(null);
(new A(<error descr="Can't pass 'string' to 'int' $x">'20'</error>))->set();
(new A(<error descr="Can't pass '?string' to 'int' $x">(new A(0))->x_or_null</error>))->set(<error descr="Can't pass 'int' to '?string' $x">9</error>);
new <error descr="No value passed for $x">A</error>;
