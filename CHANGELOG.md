# KPHPStorm Changelog

## [Unreleased]

## [1.2.8] - 2022-07-01

- adapt code for 2023.1

## [1.2.7] - 2022-12-12

- adapt code for 2022.3

## [1.2.6] - 2022-08-15

- adapt code for 2022.2
- enabled `PhpMethodOrClassCallIsNotCaseSensitiveInspection` by default
- warning level for `PhpMethodOrClassCallIsNotCaseSensitiveInspection` - **error**

## [1.2.5] - 2022-07-12

- support `@kphp-json` tags

## [1.2.4] - 2022-05-23

- support `ffi_scope` and `ffi_cdata` in phpdoc
- track calls to `end()` and `reset()`, suggesting replacing them

## [1.2.3] - 2022-04-25

- adapt code for 2022.1

## [1.2.2] - 2021-12-07

- adapt code for 2021.3

## [1.2.1] - 2021-08-06

- adapt code for 2021.2

## [1.2.0] - 2021-04-21

- adapt code for 2021.1
- `@kphp-tags` added in the past 3 months
- convert `@var` to field hint

## [1.1.0] - 2020-12-04

- better inferring for untyped arrays
- `@kphp-tags` added in the past 3 months
- able to import an undefined class
- `mixed` instead of `var`

## [1.0.0] - 2020-08-02

- custom phpdoc type parsers: support tuple, shape, var, future, arbitrary nesting, nullable types
- patched type inferring supporting tuples and shapes + hack php stdlib inferring
- complete and validate `@kphp-doc` tags
- strict typing inspections
- phpdoc simplification
- custom quick documentation and type info
- support both PHP and KPHP projects
