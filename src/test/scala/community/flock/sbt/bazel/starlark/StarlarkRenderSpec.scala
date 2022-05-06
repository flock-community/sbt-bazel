package community.flock.sbt.bazel.starlark

import higherkindness.droste.scheme

class StarlarkRenderSpec extends munit.FunSuite {
  test("render as string") {
    def module(name: String) = Map(
      "name" -> Starlark.string(name),
      "srcs" -> Starlark.function("glob", Map("include" -> Starlark.list(List(Starlark.string("src/main/scala/**/*.scala"))))),
      "resources" -> Starlark.function("glob", Map("include" -> Starlark.list(List(Starlark.string("src/main/resources/**/*.*")))))
    )

    val build = Starlark.build(
      List(
        Starlark.function("scala_library", module("mod_a")),
        Starlark.function("scala_library", module("mod_b"))
      )
    )
    val contents = scheme.cata(StarlarkAst.render).apply(build)

    assertEquals(contents,
      """scala_library(name = "mod_a", srcs = glob(include = ["src/main/scala/**/*.scala"]), resources = glob(include = ["src/main/resources/**/*.*"]))
        |scala_library(name = "mod_b", srcs = glob(include = ["src/main/scala/**/*.scala"]), resources = glob(include = ["src/main/resources/**/*.*"]))""".stripMargin)
  }
}
