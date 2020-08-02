<?php

namespace asdf;

class Holder {
    /** @var future<int> */
    public $f;
    /** @var future_queue<bool> */
    public $q;
}

function wait_queue_create(array $futures) : array {
  return $futures;
}

function demo() {
    $h = new Holder;
    $h->f = 4;
    $h->q = wait_queue_create([]);
}
