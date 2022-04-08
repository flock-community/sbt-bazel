package community.flock.sbt.renderer

import community.flock.sbt.core.{BuildArtifactId, BuildDependency, BuildDependencyConfiguration, BuildModule}
import community.flock.sbt.starlak.Starlak

class ScalaRulesRenderSpec  extends munit.FunSuite {

  val fs2 = BuildDependency("co.fs2", BuildArtifactId("fs2-core", Some("fs2-core")), "3.2.7", Some("1.6.2"), Some("2.13"))
  val itext = BuildDependency("itext", BuildArtifactId("itext", None), "1.4.1", Some("1.6.2"), None)
  val munit = BuildDependency("org.scalameta", BuildArtifactId("munit", Some("munit")), "0.7.29", Some("1.6.2"), Some("2.13"), List(BuildDependencyConfiguration.Test))

  def baseArgs(name: String, deps: List[String], plugins: List[String], dir: String) =
    Map(
      "name" -> Starlak.string(name),
      "srcs" -> Starlak.function("glob", Map("include" -> Starlak.list(List(Starlak.string(s"src/$dir/scala/**/*.scala"))))),
      "resources" -> Starlak.function("glob", Map("include" -> Starlak.list(List(Starlak.string(s"src/$dir/resources/**/*.*"))))),
      "deps" -> Starlak.list(deps.map(Starlak.string)),
      "visibility" -> Starlak.list(List(Starlak.string("//visibility:public"))),
      "plugins" -> Starlak.list(plugins.map(Starlak.string))
    )

  private val moduleName = "mod_a"
  test("output library definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Some(moduleName),
        dependencies = List(fs2, itext),
        dependsOn = Set("mod_b")
      )
    )

    val expected = Starlak.build(List(Starlak.function("scala_library", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//modules/mod_b"), Nil, "main"))))

    assertEquals(output, expected)
  }

  test("output runtime definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Some(moduleName),
        dependencies = List(fs2, itext),
        dependsOn = Set("mod_b"),
        mainClass = Some("run.Main")
      )
    )

    val expected = Starlak.build(List(Starlak.function("scala_image", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//modules/mod_b"), Nil, "main") ++ Map("main_class" -> Starlak.string("run.Main")))))

    assertEquals(output, expected)
  }

  test("output library and test definition") {
    val render = new ScalaRulesRender(BazelDepsRenderer)
    val output = render.toBuild(
      BuildModule(
        name = moduleName,
        directory = Some(moduleName),
        dependencies = List(fs2, itext, munit),
        dependsOn = Set("mod_b")
      )
    )

    val expected = Starlak.build(
      List(
        Starlak.function("scala_library", baseArgs(moduleName, List("@jvm_deps//:co_fs2_fs2_core_2_13", "@jvm_deps//:itext_itext", "//modules/mod_b"), Nil, "main")),
        Starlak.function("scala_library", baseArgs(s"${moduleName}_test", List("@jvm_deps//:org_scalameta_munit_2_13", moduleName), Nil, "test"))
      )
    )

    assertEquals(output, expected)
  }
}
