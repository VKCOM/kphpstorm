# KPHPStorm Changelog

## [Unreleased]

## [1.2.13] - 16.09.2024

- adapt code for 2024.2

## [1.2.12] - 02.05.2024

- adapt code for 2024.1

## [1.2.11] - 20.03.2024

- fixed crash when forcing types of functions

## [1.2.10] - 14.02.2024

- adapt code for 2023.3
- new inspection `KphpStrictTypesEnableInspection`

## [1.2.9] - 21.08.2023

- adapt code for 2023.2

## [1.2.8] - 14.08.2023

- adapt code for 2023.1

## [1.2.7] - 12.12.2022

- adapt code for 2022.3

## [1.2.6] - 15.08.2022

- adapt code for 2022.2
- enabled `PhpMethodOrClassCallIsNotCaseSensitiveInspection` by default
- warning level for `PhpMethodOrClassCallIsNotCaseSensitiveInspection` - **error**

## [1.2.5] - 12.07.2022

- support `@kphp-json` tags

## [1.2.4] - 23.05.2022

- support `ffi_scope` and `ffi_cdata` in phpdoc
- track calls to `end()` and `reset()`, suggesting replacing them

## [1.2.3] - 25.04.2022

- adapt code for 2022.1

## [1.2.2] - 07.12.2021

- adapt code for 2021.3

## [1.2.1] - 06.08.2021

- adapt code for 2021.2

## [1.2.0] - 21.04.2021

- adapt code for 2021.1
- `@kphp-tags` added in the past 3 months
- convert `@var` to field hint

## [1.1.0] - 04.12.2020

- better inferring for untyped arrays
- `@kphp-tags` added in the past 3 months
- able to import an undefined class
- `mixed` instead of `var`

## [1.0.0] - 02.08.2020

- custom phpdoc type parsers: support tuple, shape, var, future, arbitrary nesting, nullable types
- patched type inferring supporting tuples and shapes + hack php stdlib inferring
- complete and validate `@kphp-doc` tags
- strict typing inspections
- phpdoc simplification
- custom quick documentation and type info
- support both PHP and KPHP projects
