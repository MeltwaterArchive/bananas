# Bananas - A purely functional test framework

This project was developed during an internal hackathon at [Meltwater](https://underthehood.meltwater.com/), and is still in its infancy. You may already use bananas, but for the time being, use at your own risk :)

For more details of how bananas works, also see [underthehood.meltwater.com/bananas/](https://underthehood.meltwater.com/bananas/).

## Overview

Bananas is a core test library and plugin, and a curated set of libraries, these include:

| Library | Purpose |
| ------------- | ------------- |
| bananas-core | Core types and classes for writing bananas tests |
| bananas-plugin | Plugin which allows sbt to run bananas tests |
| pyro  | Asserts and matcher DSL |
| whales | Docker client |

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

What things you need to install the software and how to install them

**TBD**

### Installing

A step by step series of examples that tell you how to get a development env running

**TBD**

### Example

```tut:silent:nofail
import cats._, cats.implicits._, cats.data._
import cats.effect._
import com.meltwater.bananas.core.{NestedDsl, Spec, Tests}
import com.meltwater.pyro.syntax._
import fr.thomasdufour.autodiff.generic.auto._
import net.andimiller.whales._
import doobie._, doobie.implicits._
import Types.Cat

import scala.concurrent.ExecutionContext

class ExampleTest extends Spec[IO] with NestedDsl {
  implicit val timer = IO.timer(ExecutionContext.global)
  implicit val cs = IO.contextShift(ExecutionContext.global)

  def postgres[F[_]: Effect: Timer] = for {
    docker <- Docker[F]
    mysql <- docker("postgres", "11", env = Map(
      "POSTGRES_PASSWORD" -> "catpw"
    ), ports = List(5432))
    _ <- mysql.waitForPort[F](5432)
  } yield mysql

  def makeXa(ip: String) = IO {
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", s"jdbc:postgresql://${ip}:5432/postgres", "postgres", "catpw"
    )
  }

  override def tests: Tests[IO] =
    "the DAO" should(
      "provision and use a database" in postgres[IO].use { postgres =>
        for {
          xa <- makeXa(postgres.ipAddress)
          dao = new Dao[IO]
          _ <- dao.provision.transact(xa)
          insert <- dao.insert(Cat("Terry", 4)).transact(xa)
          results <- dao.findByName("Terry").transact(xa)
        } yield results.assertEqual(List(Cat("Terry", 4)))
      },
      "fail gracefully" in IO {
        val a = 2
        a.assert(s"$a should be 3")(_ == 3)
      }
    )

}
```

## Motivation

### So, why another test framework?

Most people don't like writing tests, so they're willing to put up with mutability and bad coding practices in their tests because they "aren't that important". This project is an investigation into what happens if we decide to apply the same standards to our tests that we'd use in our "real" code.

### Why not testz?

I really wanted that sbt plugin, and I disagree that frameworks can't be FP.

### Goals

* We should be able to test in any monad which has a cats-effect `Effect` (this might be relaxed to Sync later).
* No mutable state (outside of the sbt plugin)
* Preserve referential transparency
* Provide a DSL for writing tests and a DSL for making assertions, but use types that the end user can use directly

## Authors / Maintainers

* Andi Miller
* Nick Telford

See also the list of [contributors](https://github.com/meltwater/bananas/graphs/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
