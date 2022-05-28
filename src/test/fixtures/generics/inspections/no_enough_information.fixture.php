<?php
/** @noinspection PhpExpressionResultUnusedInspection */
/** @noinspection PhpUndefinedConstantInspection */

// Не шаблонный класс
class NotGeneric {}

new NotGeneric(); // ok


// Класс с одним шаблонным типом и без конструктора
/** @kphp-generic T */
class GenericT {}

<error descr="Not enough information to infer generic T">new GenericT()</error>;
<error descr="Not enough information to infer generic T">new GenericT(100)</error>; // нет зависимости от типа аргумента


// Класс с одним шаблонным типом с нестандартным именем
/** @kphp-generic TKey */
class GenericTKey {}

<error descr="Not enough information to infer generic TKey">new GenericTKey()</error>;


// Класс с двумя шаблоннымм типом
/** @kphp-generic T1, T2 */
class GenericT1T2 {}

// Выводим ошибку только для первого шаблонного типа
<error descr="Not enough information to infer generic T1">new GenericT1T2()</error>;


// Класс с одним шаблонным типом с конструктором с аргументом шаблонного типа Т
/** @kphp-generic T */
class GenericExplicitConstructorT {
  /** @param T $el */
  function __construct($el) {}
}

<error descr="Not enough information to infer generic T">new GenericExplicitConstructorT()</error>;
new GenericExplicitConstructorT(100); // в отличии от класса GenericT тут зависимость есть


// Класс с одним шаблонным типом с конструктором с еще одним шаблонным типом
/** @kphp-generic T */
class GenericExplicitConstructorT1AndT {
  /**
   * @kphp-generic T1
   * @param T1 $el
   */
  function __construct($el) {}
}

<error descr="Not enough information to infer generic T">new GenericExplicitConstructorT1AndT(100)</error>;
new GenericExplicitConstructorT1AndT/*<int, string>*/(100); // ok


// Класс с одним шаблонным типом с конструктором с еще одним шаблонным типом оба используемые в аргументах
/** @kphp-generic T */
class GenericExplicitConstructorT1AndExplicitT {
  /**
   * @kphp-generic T1
   * @param T $el
   * @param T1 $el2
   */
  function __construct($el, $el2) {}
}

<error descr="Not enough information to infer generic T1">new GenericExplicitConstructorT1AndExplicitT(100)</error>;
new GenericExplicitConstructorT1AndExplicitT(100, ""); // ok


// Класс с двумя шаблоннымм типом один из которых используется в конструкторе
/** @kphp-generic T1, T2 */
class GenericExplicitConstructorT1AndImplicitT2 {
  /** @param T1 $el */
  function __construct($el) {}
}

// Выводим ошибку только для первого шаблонного типа
<error descr="Not enough information to infer generic T1">new GenericExplicitConstructorT1AndImplicitT2()</error>;
<error descr="Not enough information to infer generic T2">new GenericExplicitConstructorT1AndImplicitT2(100)</error>;
new GenericExplicitConstructorT1AndImplicitT2<error descr="2 type arguments expected for \GenericExplicitConstructorT1AndImplicitT2.__construct">/*<int>*/</error>(100);
