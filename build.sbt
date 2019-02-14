name := "bananas"

scalaVersion := "2.12.8"

scalafmtConfig in ThisBuild := Some(file("scalafmt.conf"))

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

lazy val commonSettings = Seq(
  scalacOptions += "-Ypartial-unification",
  version := "0.6.0"
)

lazy val pyro = project.in(file("pyro")).settings(commonSettings).settings(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "sourcecode" % "0.1.5",
    "org.typelevel" %% "cats-effect" % "1.2.0",
    "fr.thomasdufour" %% "auto-diff-core" % "0.3.0-RC1",
  )
).dependsOn(bananasCore)

lazy val bananasCore = project.in(file("bananas/core")).settings(commonSettings).
  settings(
  name := "bananas-core",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.2.0",
    "co.fs2" %% "fs2-core" % "1.0.3"
  )
)

lazy val bananasPlugin = project.in(file("bananas/plugin")).settings(commonSettings).settings(
  name := "bananas-plugin",
  sbtPlugin := true,
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "fansi" % "0.2.5"
  )
).dependsOn(bananasCore)
