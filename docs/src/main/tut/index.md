---
layout: home
technologies:
 - first: ["cats", "cats can be scared by bananas"]
 - second: ["cats-effect", "scaring them is an effect"]
 - third: ["fs2", "what if we need to set up a pipeline to scare them?"]
---

# Overview

Bananas is a core test library and plugin, and a curated set of libraries, these include:

| bananas-core | Core types and classes for writing bananas tests |
| bananas-plugin | Plugin which allows sbt to run bananas tests |
| pyro  | Asserts and matcher DSL |
| whales | Docker client |

# Example

```scala
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

}```
