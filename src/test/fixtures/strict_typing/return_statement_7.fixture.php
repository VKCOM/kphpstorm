<?php

function demo1() {
    $posts = [new Post, new Post];
    $ints = [1, 2, 3];

    array_map(function(Post $post) : Post { return $post; }, $posts);
    array_map(function(Post $post) : int { <error descr="Can't return 'Post', expected 'int'">return $post;</error> }, $posts);
    array_map(function(int $int) : Post { <error descr="Can't return 'int', expected 'Post'">return $int + 1;</error> }, $ints);
    array_map(function(int $int) : int { return $int + 1; }, $ints);
}

function demo2() {
    $posts = [new Post, new Post];
    $ints = [1, 2, 3];

    array_map(fn(Post $post) : Post => $post, $posts);
    array_map(fn(Post $post) : int => <error descr="Can't return 'Post', expected 'int'">$post</error>, $posts);
    array_map(fn(int $int) : Post => <error descr="Can't return 'int', expected 'Post'">$int + 1</error>, $ints);
    array_map(fn(int $int) : int => $int, $ints);
}
