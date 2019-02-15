package com.meltwater.bananas

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import fs2._

import scala.concurrent.duration

package object core {
  type Assertions  = ValidatedNel[String, Unit]
  type Tests[F[_]] = Stream[F, Test[F]]

  object Config {
    def apply[F[_]: Functor](implicit clock: Clock[F]): F[Config] = {
      clock.monotonic(duration.NANOSECONDS).map(Config(_))
    }
  }

  case class Config(seed: Long, maxSamples: Int = 100)

  case class TestResult(labels: NonEmptyChain[String], result: Either[Throwable, Assertions])

  sealed trait Test[F[_]] {
    def labels: NonEmptyChain[String]
    def run(config: Config)(implicit F: Sync[F]): F[Assertions]
  }

  case class StandardTest[F[_]](labels: NonEmptyChain[String], test: F[Assertions]) extends Test[F] {
    def run(config: Config)(implicit F: Sync[F]): F[Assertions] = {
      test
    }
  }

  case class PropertyTest[F[_], A, T](labels: NonEmptyChain[String], gen: Gen[F, A], properties: A => F[Assertions]) extends Test[F] {

    def run(config: Config)(implicit F: Sync[F]): F[Assertions] = {
      gen
        .arbitrary(config.seed)
        .evalMap(properties)
        .take(config.maxSamples)
        .compile
        .foldMonoid
    }
  }

}
