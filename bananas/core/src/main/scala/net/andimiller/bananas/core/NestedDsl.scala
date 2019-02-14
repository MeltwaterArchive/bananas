package net.andimiller.bananas.core

import cats._, cats.implicits._, cats.data._, cats.effect._
import fs2._

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

    def in[F[_]: Functor, T](test: F[ValidatedNel[String, T]]) = Test(s, test.map(_.void))
  }
  implicit def startChain(s: String): NestedGrammar = NestedGrammar(NonEmptyChain(s))

  case class PartialGrammarNode(ss: NonEmptyChain[String], v: String) {
    private def grammar[F[_]](t: Test[F]): Test[F] =
      t.copy(labels = ss ++ NonEmptyChain.fromChainPrepend(v + " " + t.labels.head, t.labels.tail))
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

  val r = (123.validNel[String], "hello".validNel[String]).tupled
 */

}
