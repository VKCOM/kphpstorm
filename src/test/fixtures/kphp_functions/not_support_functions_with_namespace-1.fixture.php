<?php

namespace Ns1;

function end(object|array &$array): string {
    return "";
}

$arr = [];

end($arr);
<error descr="KPHP does not support reset()">reset($arr)</error>;
<error descr="KPHP does not support reset()">\reset($arr)</error>;
<error descr="KPHP does not support end()">\end($arr)</error>;
