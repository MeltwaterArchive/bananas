---
layout: docsplus
title: Asserting with Pyro
---

The Pyro DSL is intended to make it easier to construct `ValidatedNel`s, since the default cats ways aren't very handy.

To start off you'll want to import the syntax

```tut
import com.meltwater.pyro.syntax._
```

## assert

You can use the postfix `assert` method to easily assert over a value.

```tut
"hello".assert("must be uppercase")(s => s.toUpperCase == s)
```

## asserts

There's also a postfix `asserts` available to make multiple asserts over a value.

```tut
"".asserts(
  _.assert("nonempty")(_.nonEmpty),
  _.assert("uppercase")(s => s.toUpperCase == s),
  _.assert("contains 1")(_.contains('1'))
)
```

## assertEquals and diffs

This library uses [auto-diff](https://github.com/chwthewke/auto-diff) for assertEquals and diff support, if you depend on it's generic module you can get nice error messages when asserting case classes are equal like so:

```tut
import fr.thomasdufour.autodiff.generic.auto._
case class Cat(name: String, age: Int)
Cat("Terry", 4).assertEqual(Cat("Bob", 3))
```

## a note on combining asserts

Remember that since asserts actually just output a `ValidatedNel[String, ?]` you can always combine them by putting them in a tuple and calling `.tupled` like so:

```tut
import cats._, cats.implicits._
(
  10.assert("bigger than 12")(_ > 12),
  "".asserts(
    _.assert("nonEmpty")(_.nonEmpty),
    _.assert("contains 1")(_.contains('1'))
  )
).tupled
```

