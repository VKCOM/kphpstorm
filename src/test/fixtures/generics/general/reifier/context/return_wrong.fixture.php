<?php

namespace Context\Return\Wrong;

use Context\GenericClass;

interface InterfaceForGenericClass {}

/**
 * @kphp-generic T: InterfaceForGenericClass
 */
class OtherGenericClass {}

/**
 * @return GenericClass<string>
 */
function returnClassOfString() {
  return <error descr="Reified generic type for T is not within its bounds (string is not implement \Context\Return\Wrong\InterfaceForGenericClass)">new OtherGenericClass()</error>;
}


/**
 * @return <error descr="1 generic parameters expected, but 0 passed">GenericClass</error>
 */
function returnWrongClassOfString() {
  return <error descr="Not enough information to infer generic T">new GenericClass()</error>;
}
