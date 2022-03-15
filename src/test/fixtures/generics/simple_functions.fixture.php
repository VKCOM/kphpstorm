<?php /** @noinspection ALL */

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror($arg) {
  return $arg;
}

use Classes\B;
use Classes\C as GlobalC;

"Вывод шаблонного типа из параметров"; {
  "Класс в качестве типа"; {
    $a = mirror(new GlobalA());
    expr_type($a, "\GlobalA");
  }

  "Примитивные типы"; {
    $a1 = mirror("hello");
    expr_type($a1, "string");

    $a2 = mirror(1);
    expr_type($a2, "int");

    $a3 = mirror(true);
    expr_type($a3, "bool");
  }
}

"Явное указание шаблонного типа"; {
  "Класс в качестве типа"; {
    "Только имя класса"; {
      $a = mirror/*<GlobalA>*/ (new GlobalA());
      expr_type($a, "\GlobalA");
    }

    "FQN класса"; {
      $a = mirror/*<\Classes\A>*/(new \Classes\A);
      expr_type($a, "\Classes\A");
    }

    "Имя класса которое было импортировано"; {
      $a = mirror/*<B>*/(new B);
      expr_type($a, "\Classes\B");
    }

    "Имя класса которое было импортировано с алиасом"; {
      $a = mirror/*<GlobalC>*/(new GlobalC());
      expr_type($a, "\Classes\C");
    }

    "Имя класса которое было импортировано с алиасом с слешем в начале"; {
      $a = mirror/*<\GlobalC>*/(new GlobalC()); // Здесь будет ошибка, так как класс \GlobalC не будет найден
      expr_type($a, "\GlobalC");                // однако нам нужно проверить, что в таком случае тип будет также \GlobalC.
    }
  }

  "Примитивные типы"; {
    $a1 = mirror/*<string>*/("hello");
    expr_type($a1, "string");

    $a2 = mirror/*<int>*/(1);
    expr_type($a2, "int");

    $a3 = mirror/*<bool>*/(true);
    expr_type($a3, "bool");
  }

  "Сложные типы"; {
    "С примитивами"; {
      "Массивы"; {
        $a1 = mirror/*<string[]>*/(["hello"]);
        expr_type($a1, "string[]");

        $a2 = mirror/*<int[]>*/([1, 2, 3]);
        expr_type($a2, "int[]");
      }

      "Nullable"; {
        $a1 = mirror/*<?string>*/("hello");
        expr_type($a1, "string|null");

        $a2 = mirror/*<?int>*/(1);
        expr_type($a2, "null|int");
      }

      "Union"; {
        $a1 = mirror/*<int|string>*/("hello");
        expr_type($a1, "int|string");

        $a2 = mirror/*<float|bool>*/(1.5);
        expr_type($a2, "float|bool");

        $a3 = mirror/*<float|bool>*/(1.5);
        expr_type($a3, "float|bool");
      }

      "tuple"; {
        $a1 = mirror/*<tuple(int, string)>*/(tuple(1, "hello"));
        expr_type($a1, "tuple(int,string)");

        $a2 = mirror/*<tuple(int, string|float)>*/(tuple(1, "hello"));
        expr_type($a2, "tuple(int,float|string)");
      }

      "shape"; {
        $a1 = mirror/*<shape(key: int, key2: string)>*/(shape(["key" => 1, "key2" => "hello"]));
        expr_type($a1, "shape(key:int,key2:string)");

        $a2 = mirror/*<shape(key: int, key2: string|float)>*/(shape(["key" => 1, "key2" => "hello"]));
        expr_type($a2, "shape(key:int,key2:float|string)");
      }
    }

    "С классами"; {
      "Массивы"; {
        $a1 = mirror/*<\GlobalA[]>*/([new GlobalA()]);
        expr_type($a1, "\GlobalA[]");

        $a2 = mirror/*<\Classes\A[]>*/([new \Classes\A()]);
        expr_type($a2, "\Classes\A[]");

        $a3 = mirror/*<B[]>*/([new B()]);
        expr_type($a3, "\Classes\B[]");

        $a4 = mirror/*<GlobalC[]>*/([new GlobalC]);
        expr_type($a4, "\Classes\C[]");
      }

      "Nullable"; {
        $a1 = mirror/*<?\GlobalA>*/([new GlobalA()]);
        expr_type($a1, "\GlobalA|null");

        $a2 = mirror/*<?\Classes\A>*/([new \Classes\A()]);
        expr_type($a2, "null|\Classes\A");

        $a3 = mirror/*<?B>*/([new B()]);
        expr_type($a3, "null|\Classes\B");

        $a4 = mirror/*<?GlobalC>*/([new GlobalC]);
        expr_type($a4, "null|\Classes\C");
      }

      "Union"; {
        $a1 = mirror/*<\GlobalA|\Classes\A>*/("hello");
        expr_type($a1, "\GlobalA|\Classes\A");

        $a2 = mirror/*<B|GlobalC>*/(1.5);
        expr_type($a2, "\Classes\B|\Classes\C");

        $a3 = mirror/*<\Classes\A|\GlobalC>*/(1.5);
        expr_type($a3, "\GlobalC|\Classes\A");
      }

      "tuple"; {
        $a1 = mirror/*<tuple(int, string)>*/(tuple(1, "hello"));
        expr_type($a1, "tuple(int,string)");

        $a2 = mirror/*<tuple(int, string|float)>*/(tuple(1, "hello"));
        expr_type($a2, "tuple(int,float|string)");
      }

      "shape"; {
        $a1 = mirror/*<shape(key: int, key2: string)>*/(shape(["key" => 1, "key2" => "hello"]));
        expr_type($a1, "shape(key:int,key2:string)");

        $a2 = mirror/*<shape(key: int, key2: string|float)>*/(shape(["key" => 1, "key2" => "hello"]));
        expr_type($a2, "shape(key:int,key2:float|string)");
      }
    }
  }
}
