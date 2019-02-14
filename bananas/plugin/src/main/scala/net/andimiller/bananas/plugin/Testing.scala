package net.andimiller.bananas.plugin

import net.andimiller.bananas.core.{Assertions, Spec, Config}
import sbt.testing._
import cats._
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import cats.data._
import cats.effect._
import cats.effect.implicits._
import fs2._

class Bananas extends Framework {
  override def name(): String = "bananas"
  override def fingerprints(): Array[Fingerprint] =
    Array(new BananasFingerprint())
  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner = new BananasRunner
}

class BananasRunner extends Runner {
  var state = new StringBuilder()

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = taskDefs.map { t =>
    val w = Class.forName(t.fullyQualifiedName()).newInstance()
    new Task {
      override def tags(): Array[String] = Array()
      override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
        w match {
          case w: Spec[_] =>
            val F = w.F
            val config = Config(seed = System.nanoTime, maxSamples = 100) // todo: parse these from SBT
            loggers.foreach((_.info("Starting bananas test run")))
            F.toIO(w.run({ f =>
                F.flatMap(F.attempt(f.run(config)(F))) {
                  r =>
                    val result = r.asInstanceOf[Either[Throwable, Assertions]]
                    F.delay {
                      val colour =
                        result match {
                          case Left(e)           => fansi.Color.Magenta
                          case Right(Valid(_))   => fansi.Color.Green
                          case Right(Invalid(i)) => fansi.Color.Red
                        }
                      state.append(colour(s"${f.labels.toList.mkString(" ")}\n"))
                      eventHandler.handle(new Event {
                        override def fullyQualifiedName(): String =
                          t.fullyQualifiedName()

                        override def fingerprint(): Fingerprint = t.fingerprint()

                        override def selector(): Selector = new SuiteSelector

                        override def status(): Status =
                          result.toOption
                            .map(_ => Status.Success)
                            .getOrElse(Status.Failure)

                        override def throwable(): OptionalThrowable =
                          result.left.toOption
                            .map(t => new OptionalThrowable(t))
                            .getOrElse(new OptionalThrowable())

                        override def duration(): Long = -1
                      })
                      Unit
                    }
                }
              }))
              .unsafeRunSync()
        }
        Array()
      }
      override def taskDef(): TaskDef = t
    }
  }

  override def done(): String              = state.toString()
  override def remoteArgs(): Array[String] = Array()
  override def args(): Array[String]       = Array()
}

class BananasFingerprint extends SubclassFingerprint {
  override def isModule: Boolean                  = false
  override def superclassName(): String           = "net.andimiller.bananas.core.Banana"
  override def requireNoArgConstructor(): Boolean = true
}
