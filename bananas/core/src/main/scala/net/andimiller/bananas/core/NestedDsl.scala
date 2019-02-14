package net.andimiller.bananas.core

import cats._, cats.implicits._, cats.data._, cats.effect._
import fs2._

trait PropertyTests[F[_]] {
  implicit class NestedGrammar(s: NonEmptyChain[String]) {
    def forAll[A](implicit ga: Gen[F, A], F: Functor[F]): PartialPropertyGrammarNode[A] = {
      PartialPropertyGrammarNode(s)
    }

    def forAll[A, B](implicit ga: Gen[F, A], gb: Gen[F, B],F: Functor[F]): PartialPropertyGrammarNode[(A, B)] = {
      PartialPropertyGrammarNode(s)
    }

    def forAll[A, B, C](implicit ga: Gen[F, A], gb: Gen[F, B], gc: Gen[F, C], F: Functor[F]): PartialPropertyGrammarNode[(A, B, C)] = {
      PartialPropertyGrammarNode(s)
    }

    def forAll[A, B, C, D](implicit ga: Gen[F, A], gb: Gen[F, B], gc: Gen[F, C], gd: Gen[F, D], F: Functor[F]): PartialPropertyGrammarNode[(A, B, C, D)] = {
      PartialPropertyGrammarNode(s)
    }

    def forAll[A, B, C, D, E](implicit ga: Gen[F, A], gb: Gen[F, B], gc: Gen[F, C], gd: Gen[F, D], ge: Gen[F, E], F: Functor[F]): PartialPropertyGrammarNode[(A, B, C, D, E)] = {
      PartialPropertyGrammarNode(s)
    }
  }

  case class PartialPropertyGrammarNode[A](ss: NonEmptyChain[String])
                                          (implicit gen: Gen[F, A], F: Functor[F]) {
    def whenever(predicate: A => Boolean): PartialPropertyGrammarNode[A] = {
      PartialPropertyGrammarNode[A](ss)(gen.filter(predicate), F)
    }

    def apply[T](f: A => F[ValidatedNel[String, T]]): Test[F] = {
      PropertyBasedTest[F, A, T](ss, gen, f.map(_.map(_.void)))
    }
  }
}

trait NestedDsl {

  implicit class NestedGrammar(s: NonEmptyChain[String]) {
    def should    = PartialGrammarNode(s, "should")
    def must      = PartialGrammarNode(s, "must")
    def shouldNot = PartialGrammarNode(s, "should not")
    def mustNot   = PartialGrammarNode(s, "must not")
    def is        = PartialGrammarNode(s, "is")
    def isNot     = PartialGrammarNode(s, "is not")
    def are       = PartialGrammarNode(s, "are")
    def areNot    = PartialGrammarNode(s, "are not")

    def in[F[_]: Functor, T](test: F[ValidatedNel[String, T]]) = StandardTest(s, test.map(_.void))
  }
  implicit def startChain(s: String): NestedGrammar = NestedGrammar(NonEmptyChain(s))

  case class PartialGrammarNode(ss: NonEmptyChain[String], v: String) {
    private def grammar[F[_]](t: Test[F]): Test[F] = t match {
      case t: StandardTest[F] =>
        t.copy(labels = ss ++ NonEmptyChain.fromChainPrepend(v + " " + t.labels.head, t.labels.tail))
      case t: PropertyBasedTest[F, _, _] =>
        t.copy(labels = ss ++ NonEmptyChain.fromChainPrepend(v + " " + t.labels.head, t.labels.tail))
    }
    def apply[F[_]](t: Tests[F]): Tests[F]            = t.map(grammar)
    def apply[F[_]](ts: Test[F]*): Tests[F]           = apply(Stream.emits(ts))
    def apply[F[_]](t: Test[F]): Test[F]              = grammar(t)
    def apply[F[_]](s: String): NonEmptyChain[String] = ss.append(v + " " + s)
  }

  /*
  "dogs" should "be cool" in IO { ().validNel[String] }
  val cool = "be cool" in IO { ().validNel[String] }
  "dogs" should ("be cool" in IO { ().validNel[String] })

  "dogs" should (
    "be pet" in IO { "dog".validNel[String] },
    "test" in IO { ().validNel[String] }
  )

  "maths" should Stream.range(1, 1000).map { i =>
    s"double $i" in IO { (i * 2).validNel[String].ensure(NonEmptyList.of("is bigger than i"))(_ > i) }
  }

  "add" should "be monotonic" forAll[Int] whenever(i: Int => i < Int.MaxValue) { i =>
    (i + 1).assert(_ > i)
  }
  "divide" should "reduce whole numbers" forAll[Int, Int] whenever((_, y) => y != 0) whenever((x, _) => x > 0) { (x, y) =>
    (x / y).assert(_ > 0).assert(_ < x)
  }

  "add" should "be monotonic" whenever(i: Int => i < Int.MaxValue) forAll { i: Int =>
    (i + 1).assert(_ > i)
  }

  val r = (123.validNel[String], "hello".validNel[String]).tupled
 */

}
