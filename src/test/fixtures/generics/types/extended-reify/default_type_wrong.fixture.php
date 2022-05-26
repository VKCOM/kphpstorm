<?php

/**
 * <error descr="Generic parameters with a default type cannot come after parameters without a default type">@kphp-generic T1, T2 = Pair</error>
 * @param T2 $a
 * @return T1|T2
 */
function firstNotDefaultAndSecondDefault($a) { return null; }

$_ = firstNotDefaultAndSecondDefault/*<int, string>*/(100);
