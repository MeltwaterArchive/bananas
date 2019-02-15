package com.meltwater.pyro

import cats._
import cats.data._
import cats.implicits._
import fr.thomasdufour.autodiff.Difference.Pretty
import fr.thomasdufour.autodiff._
import sourcecode._

object syntax {
  implicit class assertOps[A](a: A) {
    def assert(s: String)(f: A => Boolean)(implicit line: Line, enc: Enclosing): ValidatedNel[String, A] =
      Validated.condNel(f(a), a, s"${enc.value}:${line.value} $s")
    def assertEqual(other: A)(implicit d: Diff[A], line: Line, enc: Enclosing): ValidatedNel[String, A] = {
      val diff = d(other, a)
      Validated.condNel(diff.isEmpty, a, s"${enc.value}:${line.value} ${Pretty.Colorized2.show(diff.get)}")
    }
    def asserts(vs: (A => ValidatedNel[String, _])*): ValidatedNel[String, Unit] = vs.toList.map(f => f(a).void).combineAll
  }
}
