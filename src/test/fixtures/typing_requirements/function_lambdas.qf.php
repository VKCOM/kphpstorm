<?php

function demo() {
    // lambdas don't require strict typing, but may
    $f1 = function($a, $b) { return 0; };
    $f2 = function($a2, $b):int { return 0; };

    array_map(function($a) { return $a; }, [1,2,3]);
    array_map(fn($a) => $a, [1,2,3]);
}
