package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.features.{Feature, TestSetup}
import community.flock.sbt.bazel.starlark.{Argument, StarlarkProgram, StarlarkStmt}

object PreludeRenderer {
  def apply(features: Set[Feature]): StarlarkProgram = {
    val statements: List[StarlarkStmt] = features.toList.map {
      case Feature.Docker => StarlarkStmt.Load("@io_bazel_rules_docker//scala:image.bzl", List(Argument.Literal("scala_image")))
      case Feature.Lib => StarlarkStmt.Load("@io_bazel_rules_scala//scala:scala.bzl", List(Argument.Literal("scala_library")))
      case Feature.Test(setup) =>
        val literal = setup match {
          case TestSetup.Specs2 => "scala_specs2_junit_test"
          case TestSetup.JUnit => "scala_junit_test"
          case TestSetup.Scalatest => "scala_test"
        }

        StarlarkStmt.Load("@io_bazel_rules_scala//scala:scala.bzl", List(Argument.Literal(literal)))


    }

    StarlarkProgram.of(statements)
  }
}
