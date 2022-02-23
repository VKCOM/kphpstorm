<?php

use function Ns1\end;
use function Ns1\end as reset;

$arr = [];

end($arr);
reset($arr);
<error descr="KPHP does not support reset()">\reset($arr)</error>;
<error descr="KPHP does not support end()">\end($arr)</error>;
