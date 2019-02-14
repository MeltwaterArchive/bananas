package net.andimiller.pyro

import cats._
import cats.data._
import cats.implicits._
import fr.thomasdufour.autodiff.Difference.Pretty
import fr.thomasdufour.autodiff._
import sourcecode._

object core {

  type Predicate[A] = A => ValidatedNel[String, A]

  case class Asserts[A](a: A, as: List[Predicate[A]]) {
    def assert(s: String)(f: A => Boolean)(implicit line: Line, enc: Enclosing): Asserts[A] =
      copy(as = as :+ { (a: A) =>
        Validated.condNel(f(a), a, s"${enc.value}:${line.value} $s")
      })
    def ::(p: Predicate[A]): Asserts[A] = copy(as = p :: as)
    def assertEqual(other: A)(implicit d: Diff[A], line: Line, enc: Enclosing): core.Asserts[A] = {
      val diff = d(other, a)
      copy(as = as :+ { (a: A) =>
        Validated.condNel(diff.isEmpty, a, s"${enc.value}:${line.value} ${Pretty.Colorized2.show(diff.get)}")
      })
    }
    def compile = as.map(_.apply(a).void).combineAll
  }

  object Asserts {
    implicit val assertsInvariant: Invariant[Asserts] = new Invariant[Asserts] {
      override def imap[A, B](fa: Asserts[A])(f: A => B)(g: B => A): Asserts[B] =
        Asserts(f(fa.a), fa.as.map { p => (b: B) =>
          p(g(b)).map(f)
        })
    }
    implicit val assertsSemigroupal: Semigroupal[Asserts] = new Semigroupal[Asserts] {
      override def product[A, B](fa: Asserts[A], fb: Asserts[B]): Asserts[(A, B)] =
        Asserts[(A, B)](
          (fa.a, fb.a),
          fa.as.map(pred => { p: (A, B) =>
            pred(p._1).map(a => (a, p._2))
          }) ++
            fb.as.map(pred => { p: (A, B) =>
              pred(p._2).map(b => (p._1, b))
            })
        )
    }
  }
  object syntax {
    implicit def toAsserts[A](a: A): Asserts[A] = Asserts(a, Nil)
  }
}
