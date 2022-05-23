<?php
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */

class Foo {
    public function boo() {}
}

$vec = new Vector/*<Foo>*/();
$a = $vec->get(0);



"Явные типы"; {
    $vec = new Vector/*<GlobalA>*/();
    $vec->add(new GlobalA);
    expr_type($vec->get(0), "\GlobalA");

    $filtered = $vec->filter_is_instance(GlobalD::class);
    expr_type($filtered[0], "\GlobalD");
}

"Неявные типы"; {
    $vec1 = new Vector(new GlobalA, new GlobalA);
    $vec1->add(new GlobalA);
    expr_type($vec1->get(0), "\GlobalA");

    $filtered1 = $vec1->filter_is_instance(GlobalA::class);
    expr_type($filtered1[0], "\GlobalA");

    $vec2 = new Vector(new GlobalA());
    $vec3 = new Vector(new GlobalD());

    $combine = $vec2->combine_with($vec3);
    expr_type($combine, "\Vector|\Vector(\GlobalD|\GlobalA)");

    $vec4 = new Vector(100);
    $combine1 = $vec4->combine_with($vec3);
}


