package com.meltwater.bananas.core

import cats.effect.Effect

trait Banana

abstract class Spec[F[_]](implicit val F: Effect[F]) extends Banana {
  def tests: Tests[F]
  def run(f: Test[F] => F[Unit]): F[Unit] = tests.evalMap(f).compile.drain
}
