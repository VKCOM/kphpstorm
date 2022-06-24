<?php

// Taken from kphp-polyfills
class JsonEncoder {
  public static function decode(string $json_string, string $class_name): ?object {
    return null;
  }

  public static function encode(?object $instance, int $flags = 0, array $more = []): string {
    return "";
  }
}

class ApiJsonEncoder extends JsonEncoder {
}

class FakeJsonEncoder {
  public static function decode(string $json_string, string $class_name): ?object {
    return null;
  }
}

class User {
  public string $name = "Petr";
}

$json = JsonEncoder::encode(new User());

$user1 = JsonEncoder::decode("", User::class);
$user2 = ApiJsonEncoder::decode("", User::class);
$user3 = FakeJsonEncoder::decode("", User::class);


expr_type($user1, "\User|null|object");
expr_type($user2, "\User|null|object");
expr_type($user3, "?object");
