---
layout: docsplus
title: Types
---

In this section we go on a journey through the most important types in Bananas.

## StandardTest

```scala
case class StandardTest[F[_]](labels: NonEmptyChain[String], test: F[Assertions])
```

so in Bananas, a standard test has some lebels, and an F which outputs some `Assertions`

## Assertions

```scala
type Assertions  = ValidatedNel[String, Unit]
```

`Assertions` are a `ValidatedNel` of strings, or Unit, we don't actually need an output type when tests are run, so we can just `_.void` the `ValidatedNel`.

To learn more about `Validated` and `ValidatedNel` I recommend reading the [official docs](https://typelevel.org/cats/datatypes/validated.html).

## Tests

```scala
type Tests[F[_]] = Stream[F, Test[F]]
```

A set of tests is just an fs2 stream of tests, this allows you extra flow control if you want to get fancy with resources when generating your tests, and allows you to do "table-driven" or property tests easily.

## Spec

Onto the first interface, a `Spec` is the main type of test in bananas.

```scala
abstract class Spec[F[_]](implicit val F: Effect[F]) extends Banana {
  def tests: Tests[F]
}
```

It extends `Banana`, which is our autodiscovery type for running tests in sbt.

It's defined over an `F[_]` which should be your effect type, and you need to define some tests, that's it.

Here's a simple example which doesn't use the DSL:

```tut:reset
import com.meltwater.bananas.core._
import cats._, cats.implicits._, cats.effect._, cats.data._
import fs2._

class ExampleSpec extends Spec[IO] {
  override def tests: Tests[IO] =
    Stream.emit(
      StandardTest(
        NonEmptyChain("maths", "addition"),
        IO {
          Validated.cond(1 + 1 == 2, Unit, NonEmptyList.of("1 and 1 did not equal 2"))
        }
      )
    )
}
```

This is a little bit verbose, but should give you an idea of how the types are laid out.

Here's an example with the `NestedDsl`:

```tut:reset
import com.meltwater.bananas.core._
import cats._, cats.implicits._, cats.effect._, cats.data._
import fs2._

class ExampleSpec extends Spec[IO] with NestedDsl {
  override def tests: Tests[IO] =
    "maths" should (
      "perform addition" in IO {
        Validated.cond(1 + 1 == 2, Unit, NonEmptyList.of("1 and 1 did not equal 2"))
      }
    )
}
```

And an example with the `Pyro` asserts DSL:

```tut:reset
import com.meltwater.bananas.core._
import com.meltwater.pyro.syntax._
import cats._, cats.implicits._, cats.effect._, cats.data._
import fs2._

class ExampleSpec extends Spec[IO] with NestedDsl {
  override def tests: Tests[IO] =
    "maths" should (
      "perform addition" in IO {
        (1 + 1).assertEqual(2)
      },
      "perform subtraction" in IO {
        (2 - 1).assertEqual(1)
      }
    )
}
```

To learn more about using `pyro` please see the pyro section.
