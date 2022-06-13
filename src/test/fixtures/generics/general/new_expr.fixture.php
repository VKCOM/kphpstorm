<?php

namespace NewExpr;

class TestA {}

/** @kphp-generic T */
class SimpleClass {}

$a = new SimpleClass/*<?TestA>*/();
expr_type($a, "\NewExpr\SimpleClass(?\NewExpr\TestA)");
