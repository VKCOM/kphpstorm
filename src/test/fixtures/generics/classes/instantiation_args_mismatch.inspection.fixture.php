<?php
/** @noinspection PhpExpressionResultUnusedInspection */
/** @noinspection PhpUndefinedConstantInspection */

"Не шаблонный класс"; {
    class NotGeneric {}

    new NotGeneric(); // ok
}

"Класс с одним шаблонным типом и без конструктора"; {
    /** @kphp-generic T */
    class GenericT {}

    new GenericT<error descr="1 type arguments expected for \GenericT.__construct">/*<>*/</error>();
    new GenericT/*<int>*/(); // ok
}

"Класс с двумя шаблоннымм типом"; {
    /** @kphp-generic T1, T2 */
    class GenericT1T2 {}

    new GenericT1T2<error descr="2 type arguments expected for \GenericT1T2.__construct">/*<>*/</error>();
    new GenericT1T2<error descr="2 type arguments expected for \GenericT1T2.__construct">/*<int>*/</error>();
    new GenericT1T2/*<int, string>*/(); // ok
}

"Класс с одним шаблонным типом с конструктором с еще одним шаблонным типом"; {
    /** @kphp-generic T */
    class GenericExplicitConstructorT1AndT {
        /**
         * @kphp-generic T1
         * @param T1 $el
         */
        function __construct($el) {}
    }

//    new GenericExplicitConstructorT1AndT<error1 descr="2 type arguments expected for \GenericExplicitConstructorT.__construct">/*<>*/</error>("");
//    new GenericExplicitConstructorT1AndT<error1 descr="2 type arguments expected for \GenericExplicitConstructorT.__construct">/*<int>*/</error>("");
    new GenericExplicitConstructorT1AndT/*<int, string>*/("");
}

"Методы"; {

}