<?php

trait WithMe {
    public static function getSelf() : self {}
}

class Point {
  use WithMe;

    static public function getMe() : ?self {
        $me = instance_deserialize('', self::class);
        return $me;
    }
}

class MorePoint extends Point {
    static public function getPoint1() : self {
        return self::getMe();
    }
    /** @return static */
    static public function getPoint2() {
        return new self;
    }
    static public function getPoint3() : Point {
        return self::getMe();
    }
    static public function getPoint4() : MorePoint {
        return self::getMe();
    }
}

class MorePointEx extends MorePoint {

}

/** @return MorePointEx */
function demo1() {
  return MorePointEx::getSelf();
}

/** <error descr="Actual return type is 'MorePoint'">@return</error> MorePointEx */
function demo2() {
  <error descr="Can't return 'MorePoint', expected 'MorePointEx'">return MorePoint::getSelf();</error>
}

/** @return MorePointEx */
function demo3() {
  return MorePointEx::getMe();
}

/** <error descr="Actual return type is '?MorePoint'">@return</error> MorePointEx */
function demo4() {
  <error descr="Can't return '?MorePoint', expected 'MorePointEx'">return MorePoint::getMe();</error>
}

/** @return MorePoint */
function demo5() {
  $p = MorePointEx::getMe();
  return $p;
}

/** @return MorePoint */
function demo6() {
  $p = MorePoint::getPoint2();
  return $p;
}

/** <error descr="Actual return type is 'MorePoint'">@return</error> MorePointEx */
function demo7() {
  $p = MorePoint::getPoint2();
  <error descr="Can't return 'MorePoint', expected 'MorePointEx'">return $p;</error>
}

MorePointEx::getPoint2()::exF();
MorePoint::getPoint2()::exF();      // not error, but another inspection warns in IDE
MorePoint::getSelf()::exF();        // not error, but another inspection warns in IDE

