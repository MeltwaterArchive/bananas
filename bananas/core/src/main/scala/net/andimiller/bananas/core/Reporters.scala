package net.andimiller.bananas.core

import cats._
import cats.data.Validated.Valid
import cats.implicits._
import cats.data._
import cats.effect._
import fs2._
import java.lang.Math.max

object Reporters {

  def maxDepth(rs: List[(Chain[String], Either[Throwable, ValidatedNel[String, Unit]])]) =
    Stream
      .emits(rs)
      .map(_._1.size)
      .scan(0L) { (a, i) =>
        max(a, i)
      }
      .compile
      .last
      .getOrElse(0L)

  def report(rs: List[TestResult]) = {
    Stream.emits(rs).zipWithPrevious.map {
      case (None, current) =>
        current
      case (Some(prev), current) =>
        val replaced = current.labels.toList
          .zip(prev.labels.toList)
          .takeWhile { case (a, b) => a == b }
          .map(_._1)
          .map(_.map(_ => " ").mkString(""))
        current.copy(
          labels = NonEmptyChain.fromChainUnsafe(Chain.fromSeq(replaced) ++ Chain.fromSeq(current.labels.toList.drop(replaced.length))))
    }
  }
}
