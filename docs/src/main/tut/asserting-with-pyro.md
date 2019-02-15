---
layout: docsplus
title: Asserting with Pyro
---

The Pyro DSL is intended to make it easier to construct `ValidatedNel`s, since the default cats ways aren't very handy.

To start off you'll want to import the syntax

```tut
import com.meltwater.pyro.syntax._
```

## Assert

You can use the postfix `assert` method to easily assert over a value.

```tut
"hello".assert("must be uppercase")(s => s.toUpperCase == s)
```

## assertEquals and diffs

This library uses [auto-diff](https://github.com/chwthewke/auto-diff) for assertEquals and diff support, if you depend on it's generic module you can get nice error messages when asserting case classes are equal like so:

```tut
import fr.thomasdufour.autodiff.generic.auto._
case class Cat(name: String, age: Int)
Cat("Terry", 4).assertEqual(Cat("Bob", 3))
```
