<?php
/** @noinspection FunctionUnnecessaryExplicitGenericInstantiationListInspection */
/** @noinspection PhpUndefinedFunctionInspection */
/** @noinspection PhpExpressionResultUnusedInspection */

// \GlobalA
// \Classes\A
use Classes\B;
use Classes\C as GlobalC;
use Classes\D as GlobalD;

// \GlobalD

/**
 * @kphp-generic T
 * @param T $arg
 * @return T
 */
function mirror($arg) {
    return $arg;
}

"Standalone"; {
  "Класс из глобального скоупа"; {
    $a = mirror/*<GlobalA>*/(new GlobalA());
    expr_type($a, "\GlobalA");
  }

  "Класс из пространства имен"; {
    $a = mirror/*<\Classes\A>*/(new \Classes\A());
    expr_type($a, "\Classes\A");
  }

  "Импортированный класс из пространства имен"; {
    $a = mirror/*<B>*/(new B());
    expr_type($a, "\Classes\B");
  }

  "Импортированный класс из пространства имен с алиасом"; {
    $a = mirror/*<GlobalC>*/(new GlobalC);
    expr_type($a, "\Classes\C");
  }

  "Импортированный класс из пространства имен с алиасом как у глобально класса"; {
    $a = mirror/*<GlobalD*/(new GlobalD());
    expr_type($a, "\Classes\D");
  }

  "Глобальный класс с именем как у локального алиаса для другого класса"; {
    $a = mirror/*<\GlobalD*/(new \GlobalD());
    expr_type($a, "\GlobalD");
  }
}

"Массив"; {
    "Класс из глобального скоупа"; {
        $a = mirror/*<GlobalA[]>*/([new GlobalA()]);
        expr_type($a, "\GlobalA[]");
    }

    "Класс из пространства имен"; {
        $a = mirror/*<\Classes\A[]>*/([new \Classes\A()]);
        expr_type($a, "\Classes\A[]");
    }

    "Импортированный класс из пространства имен"; {
        $a = mirror/*<B[]>*/([new B()]);
        expr_type($a, "\Classes\B[]");
    }

    "Импортированный класс из пространства имен с алиасом"; {
        $a = mirror/*<GlobalC[]>*/([new GlobalC]);
        expr_type($a, "\Classes\C[]");
    }

    "Импортированный класс из пространства имен с алиасом как у глобально класса"; {
        $a = mirror/*<GlobalD[]*/([new GlobalD()]);
        expr_type($a, "\Classes\D[]");
    }

    "Глобальный класс с именем как у локального алиаса для другого класса"; {
        $a = mirror/*<\GlobalD[]*/([new \GlobalD()]);
        expr_type($a, "\GlobalD[]");
    }
}

"Nullable"; {
    "Класс из глобального скоупа"; {
        $a = mirror/*<?GlobalA>*/(new GlobalA());
        expr_type($a, "\GlobalA|null");
    }

    "Класс из пространства имен"; {
        $a = mirror/*<?\Classes\A>*/(new \Classes\A());
        expr_type($a, "null|\Classes\A");
    }

    "Импортированный класс из пространства имен"; {
        $a = mirror/*<?B>*/(new B());
        expr_type($a, "\Classes\B|null");
    }

    "Импортированный класс из пространства имен с алиасом"; {
        $a = mirror/*<?GlobalC>*/(new GlobalC);
        expr_type($a, "null|\Classes\C");
    }

    "Импортированный класс из пространства имен с алиасом как у глобально класса"; {
        $a = mirror/*<?GlobalD*/(new GlobalD());
        expr_type($a, "null|\Classes\D");
    }

    "Глобальный класс с именем как у локального алиаса для другого класса"; {
        $a = mirror/*<?\GlobalD*/(new \GlobalD());
        expr_type($a, "null|\GlobalD");
    }
}

"Union"; {
    "Класс из глобального скоупа + Класс из пространства имен"; {
        $a = mirror/*<GlobalA|\Classes\A>*/(new GlobalA());
        expr_type($a, "\GlobalA|\Classes\A");
    }

    "Импортированный класс из пространства имен + Импортированный класс из пространства имен с алиасом"; {
        $a = mirror/*<B|GlobalC>*/(new B());
        expr_type($a, "\Classes\B|\Classes\C");
    }

    "Импортированный класс из пространства имен с алиасом как у глобально класса + Глобальный класс с именем как у локального алиаса для другого класса"; {
        $a = mirror/*<GlobalD|\GlobalD*/(new GlobalD());
        expr_type($a, "\GlobalD|\Classes\D");
    }
}

"tuple"; {
    "Класс из глобального скоупа + Класс из пространства имен"; {
        $a = mirror/*<tuple(GlobalA, \Classes\A)>*/(tuple(new GlobalA(), new \Classes\A()));
        expr_type($a, "tuple(\GlobalA,\Classes\A)");
    }

    "Импортированный класс из пространства имен + Импортированный класс из пространства имен с алиасом"; {
        $a = mirror/*<tuple(B, GlobalC)>*/(tuple(new B(), new GlobalC()));
        expr_type($a, "tuple(\Classes\B,\Classes\C)");
    }

    "Импортированный класс из пространства имен с алиасом как у глобально класса + Глобальный класс с именем как у локального алиаса для другого класса"; {
        $a = mirror/*<tuple(GlobalD, \GlobalD)*/(tuple(new GlobalD(), new \GlobalD()));
        expr_type($a, "tuple(\Classes\D,\GlobalD)");
    }
}

"shape"; {
    "Класс из глобального скоупа + Класс из пространства имен"; {
        $a = mirror/*<shape(key1: GlobalA, key2: \Classes\A)>*/(shape(["key1" => new GlobalA, "key2" => new \Classes\A]));
        expr_type($a, "shape(key1:\GlobalA,key2:\Classes\A)");
    }

    "Импортированный класс из пространства имен + Импортированный класс из пространства имен с алиасом"; {
        $a = mirror/*<shape(key1: B, key2: GlobalC)>*/(shape(["key1" => new B(), "key2" => new GlobalC()]));
        expr_type($a, "shape(key1:\Classes\B,key2:\Classes\C)");
    }

    "Импортированный класс из пространства имен с алиасом как у глобально класса + Глобальный класс с именем как у локального алиаса для другого класса"; {
        $a = mirror/*<shape(key1: GlobalD, key2: \GlobalD)*/(shape(["key1" => new GlobalD(), "key2" => new \GlobalD()]));
        expr_type($a, "shape(key1:\Classes\D,key2:\GlobalD)");
    }
}

"Mixed"; {
    $a = mirror/*<tuple(GlobalA[], \Classes\A|GlobalC)>*/(tuple([new GlobalA()], new \Classes\A()));
    expr_type($a, "tuple(\GlobalA[],\Classes\C|\Classes\A)");

    $a1 = mirror/*<shape(key1: tuple(GlobalA[], \Classes\A|GlobalC), key2: ?\GlobalD[])*/(shape(["key1" => $a, "key2" => [new \GlobalD()]]));
    expr_type($a1, "shape(key1:tuple(\GlobalA[],\Classes\C|\Classes\A),key2:?\GlobalD[])");
}
