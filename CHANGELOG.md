# KPHPStorm Changelog

## [Unreleased]

## [1.2.5]

- support `@kphp-json` tags

## [1.2.4]

- support `ffi_scope` and `ffi_cdata` in phpdoc
- track calls to `end()` and `reset()`, suggesting replacing them

## [1.2.3]

- adapt code for 2022.1

## [1.2.2]

- adapt code for 2021.3

## [1.2.1]

- adapt code for 2021.2

## [1.2.0]

- adapt code for 2021.1
- `@kphp-tags` added in the past 3 months
- convert `@var` to field hint

## [1.1.0]

- better inferring for untyped arrays
- `@kphp-tags` added in the past 3 months
- able to import an undefined class
- `mixed` instead of `var`

## [1.0.0]

- custom phpdoc type parsers: support tuple, shape, var, future, arbitrary nesting, nullable types
- patched type inferring supporting tuples and shapes + hack php stdlib inferring
- complete and validate `@kphp-doc` tags
- strict typing inspections
- phpdoc simplification
- custom quick documentation and type info
- support both PHP and KPHP projects
