# **Extended Number Converter**
Extended version of https://github.com/dethlex/NumberConverter

The plugin to convert numbers from one numeral system to another in any JetBrains IDE.
For floating point numbers, the fractional part will be discarded.

### Features:
- Parsing and transforming for bit or
- Parsing and transforming for bit shits
- Support for dart, go, groovy, java, javaScript, kotlin, php, python, ruby, rust, scala
- big integers
- engineering types
- negative conversion (bit shifting)
- multi carets
- shortcuts

### Current supported numeral transforms:
- To OrProduct (120 => 1<<6 | 1<<5 | 1<<4 | 1<<3)
- To LeftShift (120 => 0b1111<<3)
- To Decimal
- To Hexadecimal
- To Octal
- To Binary
