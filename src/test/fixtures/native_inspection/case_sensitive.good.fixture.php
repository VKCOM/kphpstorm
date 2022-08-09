<?php

class AbC {}

$a = new <error descr="Case in class usage doesn't match the case in declaration">aBc</error>();
