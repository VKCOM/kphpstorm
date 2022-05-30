<?php

namespace Fields;

class Foo {
  public function fooMethod(): ?int {}
}

/**
 * @kphp-generic T
 */
class GenericClass {
  /** @var T */
  public $plain_field;

  /** @var ?T */
  public $nullable_field = null;

  /** @var T[] */
  public $array_field = [];

  /** @var T|string */
  public $union_field = "";

  /** @var tuple(T, int, string, T) */
  public $tuple_field = [];

  /** @var shape(key1: T) */
  public $shape_field = [];

  /** @var class-string<T> */
  public $class_string_field;
}

$a = new GenericClass/*<Foo>*/ ();

$a->plain_field->fooMethod();
expr_type($a->plain_field, "\Fields\Foo");

$a->nullable_field->fooMethod();
expr_type($a->nullable_field, "?\Fields\Foo");

$a->array_field[0]->fooMethod();
// TODO: здесь не должно быть any[]
expr_type($a->array_field, "\Fields\Foo[]|any[]");

$a->union_field->fooMethod();
expr_type($a->union_field, "\Fields\Foo|string");

$a->tuple_field[0]->fooMethod();
// TODO: здесь не должно быть any[] и any
expr_type($a->tuple_field, "any[]|tuple(\Fields\Foo,int,string,\Fields\Foo)");
expr_type($a->tuple_field[0], "\Fields\Foo|any");
expr_type($a->tuple_field[1], "any|int");
expr_type($a->tuple_field[2], "any|string");
expr_type($a->tuple_field[3], "\Fields\Foo|any");

$a->shape_field["key1"]->fooMethod();
// TODO: здесь не должно быть any[] и any
expr_type($a->shape_field, "any[]|shape(key1:\Fields\Foo)");

$a->class_string_field->fooMethod();
expr_type($a->class_string_field, "class-string(\Fields\Foo)");

/** @return GenericClass<Foo> */
function getGenericClass(): GenericClass { return new GenericClass(); }

expr_type(getGenericClass()->union_field->fooMethod(), "?int");
expr_type(getGenericClass()->nullable_field->fooMethod(), "?int");
expr_type(getGenericClass()->array_field[0]->fooMethod(), "?int");
expr_type(getGenericClass()->tuple_field[0]->fooMethod(), "?int");
expr_type(getGenericClass()->shape_field["key1"]->fooMethod(), "?int");
