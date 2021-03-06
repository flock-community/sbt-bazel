package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.core.{BuildArtifactId, BuildDependency, BuildDependencyConfiguration, BuildModule, BuildProjectDependency}
import community.flock.sbt.bazel.features.{Feature, TestSetup}
import community.flock.sbt.bazel.starlark.{Starlark, StarlarkProgram}

import java.nio.file.Path


class ScalaRulesRenderSpec  extends munit.FunSuite {

  val fs2 = BuildDependency("co.fs2", BuildArtifactId("fs2-core", Some("fs2-core")), "3.2.7", Some("1.6.2"), Some("2.13"), Set(BuildDependencyConfiguration.Compile))
  val itext = BuildDependency("itext", BuildArtifactId("itext", None), "1.4.1", Some("1.6.2"), None, Set(BuildDependencyConfiguration.Compile))
  val munit = BuildDependency("org.scalameta", BuildArtifactId("munit", Some("munit")), "0.7.29", Some("1.6.2"), Some("2.13"), Set(BuildDependencyConfiguration.Test))

  def baseArgs(name: String, deps: List[String], plugins: List[String], dir: String) =
    Map(
      "name" -> Starlark.string(name).expr,
      "srcs" -> Starlark.functionNamed("glob", Map("include" -> Starlark.list(List(Starlark.string(s"src/$dir/scala/**/*.scala"))).expr)),
      "resources" -> Starlark.functionNamed("glob", Map("include" -> Starlark.list(List(Starlark.string(s"src/$dir/resources/**/*.*"))).expr)),
      "deps" -> Starlark.list(deps.map(Starlark.string)).expr,
      "visibility" -> Starlark.list(List(Starlark.string("//visibility:public"))).expr,
      "plugins" -> Starlark.list(plugins.map(Starlark.string)).expr,
      "scalacopts" -> Starlark.list(List.empty).expr,
      "scalac_jvm_flags" -> Starlark.list(List.empty).expr
    )

  private val moduleName = "mod_a"
  test("output library definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Path.of(moduleName),
        dependencies = List(fs2, itext),
        dependsOn = Set(BuildProjectDependency("mod_b", "mod_b"))
      )
    )

    val expected = StarlarkProgram(
      List(
        Starlark.functionNamed("scala_library", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//mod_b:mod_b"), Nil, "main")).stmt
      )
    )

    assertEquals(output, expected)
  }

  test("output runtime definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Path.of(moduleName),
        dependencies = List(fs2, itext),
        dependsOn = Set(BuildProjectDependency("mod_b", "mod_b")),
        mainClass = Some("run.Main")
      )
    )

    val expected = StarlarkProgram(
      List(
        Starlark.functionNamed(
          "scala_image",
          baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//mod_b:mod_b"), Nil, "main") ++ Map("main_class" -> Starlark.string("run.Main").expr)).stmt
      )
    )

    assertEquals(output, expected)
  }

  test("output library and junit test definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Path.of(moduleName),
        dependencies = List(fs2, itext, munit),
        dependsOn = Set(BuildProjectDependency("mod_b", "mod_b")),
        features = List(Feature.Test(TestSetup.JUnit))
      )
    )

    val expected = StarlarkProgram(
      List(
        Starlark.functionNamed("scala_library", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//mod_b:mod_b"), Nil, "main")).stmt,
        Starlark.functionNamed("scala_junit_test", baseArgs(s"${moduleName}_test", List("@jvm_deps//:org_scalameta_munit_2_13", s":$moduleName"), Nil, "test") ++ Map("suffixes" -> Starlark.list(List(Starlark.string("Spec"), Starlark.string("Test"))).expr)).stmt
      )
    )

    assertEquals(output, expected)
  }

  test("output library and specs2 test definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Path.of(moduleName),
        dependencies = List(fs2, itext, munit),
        dependsOn = Set(BuildProjectDependency("mod_b", "mod_b")),
        features = List(Feature.Test(TestSetup.Specs2))
      )
    )

    val expected = StarlarkProgram(
      List(
        Starlark.functionNamed("scala_library", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//mod_b:mod_b"), Nil, "main")).stmt,
        Starlark.functionNamed("scala_specs2_junit_test", baseArgs(s"${moduleName}_test", List("@jvm_deps//:org_scalameta_munit_2_13", s":$moduleName"), Nil, "test") ++ Map("suffixes" -> Starlark.list(List(Starlark.string("Spec"), Starlark.string("Test"))).expr)).stmt
      )
    )

    assertEquals(output, expected)
  }

  test("output library and scalatest test definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Path.of(moduleName),
        dependencies = List(fs2, itext, munit),
        dependsOn = Set(BuildProjectDependency("mod_b", "mod_b")),
        features = List(Feature.Test(TestSetup.Scalatest))
      )
    )

    val expected = StarlarkProgram(
      List(
        Starlark.functionNamed("scala_library", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//mod_b:mod_b"), Nil, "main")).stmt,
        Starlark.functionNamed("scala_test", baseArgs(s"${moduleName}_test", List("@jvm_deps//:org_scalameta_munit_2_13", s":$moduleName"), Nil, "test")).stmt
      )
    )

    assertEquals(output, expected)
  }
}
