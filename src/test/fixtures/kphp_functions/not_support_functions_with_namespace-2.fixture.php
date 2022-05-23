<?php

namespace Ns2;

function reset(): string {
    return "";
}

$arr = [];

reset();
<error descr="KPHP does not support end()">end($arr)</error>;
\Ns1\end($arr);
<error descr="KPHP does not support reset()">\reset($arr)</error>;
