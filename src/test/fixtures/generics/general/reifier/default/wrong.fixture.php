<?php

/**
 * <error descr="Generic parameters with a default type cannot come before parameters without a default type">@kphp-generic T1 = Pair, T2</error>
 * @param T2 $a
 * @return T1|T2
 */
function firstNotDefaultAndSecondDefault($a) { return null; }

$_ = firstNotDefaultAndSecondDefault/*<int, string>*/("100");
