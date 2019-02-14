package net.andimiller.bananas.plugin

import net.andimiller.bananas.core.{Assertions, Reporters, Spec, TestResult}
import sbt.testing._
import cats._
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import cats.data._
import cats.effect._
import fs2._

import scala.collection.mutable

class Bananas extends Framework {
  override def name(): String = "bananas"
  override def fingerprints(): Array[Fingerprint] =
    Array(new BananasFingerprint())
  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner = new BananasRunner
}

class BananasRunner extends Runner {
  val state = mutable.ListBuffer.empty[TestResult]

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = taskDefs.map { t =>
    val w = Class.forName(t.fullyQualifiedName()).newInstance()
    new Task {
      override def tags(): Array[String] = Array()
      override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
        w match {
          case w: Spec[_] =>
            val F = w.F
            loggers.foreach((_.info("Starting bananas test run")))
            F.toIO(w.run({ f =>
                F.flatMap(F.attempt(f.test)) {
                  r =>
                    val result = r.asInstanceOf[Either[Throwable, Assertions]]
                    val testResult = TestResult(f.labels, result)
                    F.delay {
                      state.append(testResult)

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

  private def indent(s: List[String], initial: String, prefixes: String, delim: String): String = {
    (s.headOption.map(initial + _).toList ++ s.tail.map(prefixes + _)).mkString(delim)
  }

  override def done(): String              = {
    Reporters.report(state.toList).toList.map { r =>
      val colour = r.result match {
        case Left(e)           => fansi.Color.Magenta
        case Right(Valid(_))   => fansi.Color.Green
        case Right(Invalid(i)) => fansi.Color.Red
      }
      val labels = r.labels.toList.mkString(" ")
      val error = r.result match {
        case Left(e) => Some(indent(e.getStackTrace.toList.map(_.toString), "  * ", "    ", "\n"))
        case Right(Valid(_)) => None
        case Right(Invalid(is)) => Some(indent(is.toList, "  * ", "  * ", "\n"))
      }
      (labels + error.map("\n" + _).getOrElse("")).split('\n').map(s => colour(s)).mkString("\n")
    }.mkString("\n")
  }
  override def remoteArgs(): Array[String] = Array()
  override def args(): Array[String]       = Array()
}

class BananasFingerprint extends SubclassFingerprint {
  override def isModule: Boolean                  = false
  override def superclassName(): String           = "net.andimiller.bananas.core.Banana"
  override def requireNoArgConstructor(): Boolean = true
}
