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

