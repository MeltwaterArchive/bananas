name := "bananas"

scalaVersion := "2.12.8"

scalafmtConfig in ThisBuild := Some(file("scalafmt.conf"))

lazy val commonSettings = Seq(
  scalacOptions += "-Ypartial-unification",
  version := "0.6.0",
  organization := "com.meltwater",
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
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

lazy val docs = project.in(file("docs")).settings(commonSettings).enablePlugins(MicrositesPlugin).settings(
  micrositeName := "Bananas",
  micrositeDescription := "A purely functional test framework",
  micrositeUrl := "https://underthethood.meltwater.com",
  micrositeBaseUrl := "/bananas",
  micrositeGithubOwner := "meltwater",
  micrositeGithubRepo := "bananas",
  micrositeAuthor := "Meltwater",
  micrositePalette := Map(
    "brand-primary"   -> "#FFDE25",
    "brand-secondary" -> "#403709",
    "brand-tertiary"  -> "#7F6F13",
    "gray-dark"       -> "#453E46",
    "gray"            -> "#837F84",
    "gray-light"      -> "#E3E2E3",
    "gray-lighter"    -> "#F4F3F4",
    "white-color"     -> "#FFFFFF"
  ),
  micrositeDocumentationUrl := "/bananas/introduction.html",
  scalacOptions in Tut += "-Ypartial-unification",
).dependsOn(pyro).dependsOn(bananasCore)
