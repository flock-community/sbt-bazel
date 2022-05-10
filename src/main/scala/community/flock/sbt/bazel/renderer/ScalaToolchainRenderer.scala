package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.starlark.{Argument, Starlark, StarlarkProgram, StarlarkStmt}

object ScalaToolchainRenderer {

  def render(scalacJvmFlags: List[String]): StarlarkProgram =
    StarlarkProgram.of(
      StarlarkStmt.Load("@io_bazel_rules_scala//scala:scala_toolchain.bzl", List(Argument.Literal("scala_toolchain"))),
      StarlarkStmt.Load("@io_bazel_rules_scala//scala:providers.bzl", List(Argument.Literal("declare_deps_provider"))),
      StarlarkStmt.Expr(
        Starlark.functionNamed("scala_toolchain",
          Map(
            "name" -> Starlark.string("scala").expr,
            "strict_deps_mode" -> Starlark.string("off").expr,
            "unused_dependency_checker_mode" -> Starlark.string("off").expr,
            "scalac_jvm_flags" -> Starlark.list(scalacJvmFlags.map(Starlark.string)).expr,
            "dependency_mode" -> Starlark.string("transitive").expr,
            "dependency_tracking_method" -> Starlark.string("ast").expr,
            "visibility" -> Starlark.list(List(Starlark.string("//visibility:public"))).expr,
          )
        )
      ),
      StarlarkStmt.Expr(
        Starlark.functionNamed("toolchain",
          Map(
            "name" -> Starlark.string("scala_toolchain").expr,
            "toolchain_type" -> Starlark.string("@io_bazel_rules_scala//scala:toolchain_type").expr,
            "toolchain" -> Starlark.string("scala").expr,
            "visibility" -> Starlark.list(List(Starlark.string("//visibility:public"))).expr,
          )
        )
      )
    )

}
