package com.meltwater.bananas.core

import cats._
import cats.implicits._
import cats.effect._
import fs2._

object Gen extends GenInstances {
  def apply[F[_], A](f: Long => Stream[F, A]): Gen[F, A] = (seed: Long) => f(seed)
  def const[A](x: A): Gen[Pure, A]                       = (_: Long) => Stream.emit(x)

  def oneOf[F[_]: Sync, A](x1: A, x2: A, xs: A*): Gen[F, A] = oneOf(x1 +: x2 +: xs)
  def oneOf[F[_], A](xs: Seq[A])(implicit F: Sync[F]): Gen[F, A] = (seed: Long) => {
    Stream
      .eval(F.delay(new scala.util.Random(seed)))
      .flatMap { rng =>
        Stream.emits(rng.shuffle(xs))
      }
  }
}

trait Gen[+F[_], +A] {
  def arbitrary(seed: Long): Stream[F, A]

  def filter(f: A => Boolean): Gen[F, A] = (seed: Long) => arbitrary(seed).filter(f)
  def map[B](f: A => B): Gen[F, B]       = (seed: Long) => arbitrary(seed).map(f)
  def flatMap[F2[x] >: F[x], B](f: A => Gen[F2, B]): Gen[F2, B] =
    (seed: Long) => arbitrary(seed).flatMap(f.andThen(_.arbitrary(seed)))
}

trait GenInstances {

  implicit def genMonad[F[_]]: Monad[Gen[F, ?]] = new Monad[Gen[F, ?]] {
    override def flatMap[A, B](fa: Gen[F, A])(f: A => Gen[F, B]): Gen[F, B] = {
      fa.flatMap(f)
    }

    override def map[A, B](fa: Gen[F, A])(f: A => B): Gen[F, B] = {
      fa.map(f)
    }

    override def tailRecM[A, B](a: A)(f: A => Gen[F, Either[A, B]]): Gen[F, B] =
      (seed: Long) => Monad[Stream[F, ?]].tailRecM[A, B](a)(f.map(_.arbitrary(seed)))

    override def pure[A](x: A): Gen[F, A] = Gen.const(x)
  }

  private def numeric[F[_], N](min: N, max: N, next: scala.util.Random => N)(implicit N: Numeric[N], F: Sync[F]): Gen[F, N] =
    (seed: Long) =>
      Stream(N.zero, min, max, N.negate(N.one), N.one) ++
        Stream
          .eval(F.delay(new scala.util.Random(seed)))
          .flatMap { r =>
            def loop: Stream[F, N] = Stream.emit(next(r)) ++ loop
            loop
          }

  implicit def intGen[F[_]: Sync]: Gen[F, Int] = numeric(Int.MinValue, Int.MaxValue, _.nextInt)

  implicit def longGen[F[_]: Sync]: Gen[F, Long] = numeric(Long.MinValue, Long.MaxValue, _.nextLong)

  implicit def charGen[F[_]: Sync]: Gen[F, Char] = {
    def nextChar(r: scala.util.Random): Char = {
      (r.nextInt(Char.MaxValue - Char.MinValue) + Char.MinValue).toChar
    }
    numeric(Char.MinValue, Char.MaxValue, nextChar)
  }

  implicit def stringGen[F[_]](implicit F: Sync[F], sizes: Gen[F, Int]): Gen[F, String] =
    (seed: Long) =>
      Stream("", " ", "\n", "\t") ++
        Stream
          .eval(F.delay(new scala.util.Random(seed)))
          .flatMap { r =>
            sizes.arbitrary(seed).flatMap { size =>
              def loop: Stream[F, String] = Stream.emit(r.nextString(size)) ++ loop
              loop
            }
          }

  implicit def optionGen[F[_], A](implicit gen: Gen[F, A]): Gen[F, Option[A]] =
    (seed: Long) => Stream.emit(None) ++ gen.arbitrary(seed).map(_.some)

  implicit def listGen[F[_]: Sync, A](implicit gen: Gen[F, A], sizes: Gen[F, Int]): Gen[F, List[A]] =
    (seed: Long) =>
      Stream.emit(Nil).covaryAll[F, List[A]] ++ sizes.arbitrary(seed).flatMap { size =>
        Stream.eval(gen.arbitrary(seed).take(size).compile.toList)
      }

  implicit def tuple2Gen[F[_], A, B](implicit ga: Gen[F, A], gb: Gen[F, B]): Gen[F, (A, B)] =
    (ga, gb).tupled
  implicit def tuple3Gen[F[_], A, B, C](implicit ga: Gen[F, A], gb: Gen[F, B], gc: Gen[F, C]): Gen[F, (A, B, C)] =
    (ga, gb, gc).tupled
  implicit def tuple4Gen[F[_], A, B, C, D](implicit ga: Gen[F, A], gb: Gen[F, B], gc: Gen[F, C], gd: Gen[F, D]): Gen[F, (A, B, C, D)] =
    (ga, gb, gc, gd).tupled
  implicit def tuple5Gen[F[_], A, B, C, D, E](implicit ga: Gen[F, A],
                                              gb: Gen[F, B],
                                              gc: Gen[F, C],
                                              gd: Gen[F, D],
                                              ge: Gen[F, E]): Gen[F, (A, B, C, D, E)] =
    (ga, gb, gc, gd, ge).tupled
}

