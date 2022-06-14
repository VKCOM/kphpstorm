# KPHPStorm plugin for PhpStorm

[![Build](https://github.com/unserialize/kphpstorm/workflows/Build/badge.svg)](https://github.com/unserialize/kphpstorm/workflows/Build/badge.svg)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE)
[![Total Downloads](https://img.shields.io/jetbrains/plugin/d/14814)](https://plugins.jetbrains.com/plugin/14814-kphpstorm)
[![Latest Version](https://img.shields.io/jetbrains/plugin/v/14814)](https://plugins.jetbrains.com/plugin/14814-kphpstorm)

PhpStorm plugin that makes IDE understand KPHP specifics.
[JetBrains repository](https://plugins.jetbrains.com/plugin/14814-kphpstorm/)

KPHP is a PHP compiler. 
Its goal is to execute PHP code much faster. 
It supports a strict subset of PHP and brings new types, functions, annotations and web server runtime.


## Brief info

* extended types in phpdoc: arbirtary nesting, ?nullable, tuples, shapes and other KPHP types

  <img width="295" alt="phpdoc types" src="https://user-images.githubusercontent.com/67757852/87846922-ed325000-c8fd-11ea-9f91-8c5610f968da.png">

* extended type inferring with per-key access completion and stdlib enhancements
  
  <img width="225" alt="extended tinf" src="https://user-images.githubusercontent.com/67757852/87847098-68483600-c8ff-11ea-962c-905f28846156.png">

* @kphp- doc tags autocomplete and validation

  <img width="302" alt="@kphp doc tag" src="https://user-images.githubusercontent.com/67757852/87847142-cffe8100-c8ff-11ea-9b04-c42e725abbde.png">
  
* strict type checking in function calls, assignments, return statements, array access, etc 

  <img width="270" alt="strict typing" src="https://user-images.githubusercontent.com/67757852/87847273-0ab4e900-c901-11ea-934d-0612e7397bad.png">
  
* ... and much more!  


## Detailed info

#### **[Landing page (in Russian)](https://unserialize.github.io/kphpstorm/)**


## Useful for plugin developers

Provided code can help IDEA/PhpStorm plugin developers in various aspects: how to provide extended type inferring, parse and colorize custom doc tags, traverse PSI structure, handle stubs metadata and lots of corner cases.

An interesting fact is that plugin disables lots of features in plain PHP projects (not KPHP-based).

Feel free to examine this code, it contains lots of necessary comments.   


## Questions and contribution

Please contact [vk.com/kphp](https://vk.com/kphp) or VK Team.
