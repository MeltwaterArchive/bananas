package net.andimiller.bananas

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import fs2._

package object core {
  type Assertions = ValidatedNel[String, Unit]
  case class Test[F[_]](labels: NonEmptyChain[String], test: F[Assertions])
  case class TestResult(labels: NonEmptyChain[String], result: Either[Throwable, Assertions])
  type Tests[F[_]] = Stream[F, Test[F]]
}
