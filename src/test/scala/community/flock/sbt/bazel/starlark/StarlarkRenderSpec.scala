package community.flock.sbt.bazel.starlark

class StarlarkRenderSpec extends munit.FunSuite {
  test("render as string") {
    def module(name: String) = Map(
      "name" -> Starlark.string(name).expr,
      "srcs" -> Starlark.functionNamed("glob", Map("include" -> Starlark.list(List(Starlark.string("src/main/scala/**/*.scala"))).expr)),
      "resources" -> Starlark.functionNamed("glob", Map("include" -> Starlark.list(List(Starlark.string("src/main/resources/**/*.*"))).expr))
    )

    val build = StarlarkProgram(
      List(
        Starlark.functionNamed("scala_library", module("mod_a")).stmt,
        Starlark.functionNamed("scala_library", module("mod_b")).stmt
      )
    )
    val contents = StarlarkProgram.show(build)

    assertEquals(contents,
      """scala_library(name = "mod_a", srcs = glob(include = ["src/main/scala/**/*.scala"]), resources = glob(include = ["src/main/resources/**/*.*"]))
        |scala_library(name = "mod_b", srcs = glob(include = ["src/main/scala/**/*.scala"]), resources = glob(include = ["src/main/resources/**/*.*"]))""".stripMargin)
  }
}
