<?php

function demo1() {
    $posts = [new Post, new Post];
    $ints = [1, 2, 3];

    // such argument mismatch are not detected, because there are no function calls
    // (but of course they are detected by kphp)
    array_map(function(Post $post) { return $post; }, $posts);
    array_map(function(int $post) { return $post; }, $posts);
    array_map(function(Post $int) { return $int; }, $ints);
    array_map(function(int $int) { return $int; }, $ints);
}
