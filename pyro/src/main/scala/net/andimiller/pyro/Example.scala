package net.andimiller.pyro

import net.andimiller.pyro.core._
import net.andimiller.pyro.core.syntax._
import cats._, cats.implicits._

object Example extends App {

  def validate(i: Int) =
    i.assert("must be over 10") { _ > 10 }
      .assert("must be even") { _ % 2 == 0 }

  def validateString(s: String) =
    s.assert("must not be empty") { _.nonEmpty }
      .assert("must be lowercase") { s =>
        s.toLowerCase == s
      }

  //val result = (validate(3), validateString("")).tupled
}
