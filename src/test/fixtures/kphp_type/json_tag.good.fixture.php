<?php

// Taken from kphp-polyfills
class JsonEncoder {
  public static function decode(string $json_string, string $class_name): ?object {
    return null;
  }
}

class BaseJsonEncoder extends JsonEncoder {
}

class ApiJsonEncoder extends BaseJsonEncoder {
}

class FakeJsonEncoder {
  public static function decode(string $json_string, string $class_name): ?object {
    return null;
  }
}

class User {
  public string $name = "Petr";
}

$user1 = JsonEncoder::decode("", User::class);
$user2 = BaseJsonEncoder::decode("", User::class);
$user3 = ApiJsonEncoder::decode("", User::class);
$user4 = FakeJsonEncoder::decode("", User::class);

expr_type($user1, "?\User");
expr_type($user2, "?\User");
expr_type($user3, "?\User");
expr_type($user4, "null");
