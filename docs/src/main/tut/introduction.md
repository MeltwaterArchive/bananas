---
layout: docsplus
title: Introduction
---

## Motivation

### So, why another test framework?

Most people don't like writing tests, so they're willing to put up with mutability and bad coding practices in their tests because they "aren't that important", this project is an investigation into what happens if we decide to apply the same standards to our tests that we'd use in our "real" code.

### Why not testz?

I really wanted that sbt plugin, and I disagree that frameworks can't be FP.

## Goals

* We should be able to test in any monad which has a cats-effect `Effect` (this might be relaxed to Sync later).
* No mutable state (outside of the sbt plugin)
* Preserve referential transparency
* Provide a DSL for writing tests and a DSL for making assertions, but use types that the end user can use directly
