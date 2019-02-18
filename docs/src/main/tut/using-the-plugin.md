---
layout: docsplus
title: Using the Plugin
---

Bananas provides an sbt plugin which can run your tests, you depend on it by adding a `bananas.sbt` to your `project` folder and putting the following in it:

```scala
addSbtPlugin("com.meltwater" % "bananas-plugin" % "version")
testFrameworks += new TestFramework("com.meltwater.bananas.plugin.Bananas")
```

Then adding `bananas-core` and any plugins you'd like to your build.sbt:

```scala
libraryDependencies += "com.meltwater" % "bananas-core" % "version"
libraryDependencies += "com.meltwater" % "pyro" % "version"
```

