package net.andimiller.pyro

import cats._
import cats.data._
import cats.implicits._

object core {

  type Predicate[A] = A => ValidatedNel[String, A]
  case class Asserts[A](a: A, as: List[Predicate[A]]) {
    def assert(s: String)(f: A => Boolean): Asserts[A] =
      copy(as = { (a: A) =>
        Validated.condNel(f(a), a, s)
      } :: as)
  }

  object syntax {

    implicit class InitialAssertChainSyntax[A](a: A) {
      def assert(s: String)(f: A => Boolean): Asserts[A] =
        Asserts(
          a,
          List((a: A) => Validated.condNel(f(a), a, s))
        )
    }

  }
}
