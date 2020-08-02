<?php

class A {
    var int $i;
    var $any;
}

function demo(int $s) {
}

demo(<error descr="Can't pass 'A' to 'int' $s">new A</error>);
demo(5);
demo(<error descr="Can't pass 'int[]' to 'int' $s">[1,2]</error>);
demo(<error descr="Can't pass 'false' to 'int' $s">false</error>);
demo((new A)->i);
demo((new A)->any);

