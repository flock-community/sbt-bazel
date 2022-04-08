package community.flock.sbt.starlak

import higherkindness.droste.scheme

class StarlakRenderSpec extends munit.FunSuite {
  test("render as string") {
    def module(name: String) = Map(
      "name" -> Starlak.string(name),
      "srcs" -> Starlak.function("glob", Map("include" -> Starlak.list(List(Starlak.string("src/main/scala/**/*.scala"))))),
      "resources" -> Starlak.function("glob", Map("include" -> Starlak.list(List(Starlak.string("src/main/resources/**/*.*")))))
    )

    val build = Starlak.build(
      List(
        Starlak.function("scala_library", module("mod_a")),
        Starlak.function("scala_library", module("mod_b"))
      )
    )
    val contents = scheme.cata(StarlakAst.render).apply(build)

    assertEquals(contents,
      """scala_library(name = "mod_a", srcs = glob(include = ["src/main/scala/**/*.scala"]), resources = glob(include = ["src/main/resources/**/*.*"]))
        |scala_library(name = "mod_b", srcs = glob(include = ["src/main/scala/**/*.scala"]), resources = glob(include = ["src/main/resources/**/*.*"]))""".stripMargin)
  }
}
