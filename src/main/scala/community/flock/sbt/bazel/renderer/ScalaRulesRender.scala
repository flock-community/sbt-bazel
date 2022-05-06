package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.core.{BuildDependency, BuildModule}
import community.flock.sbt.bazel.starlark.{Starlark, StarlarkAst}
import higherkindness.droste.scheme
import sbt.*

final class ScalaRulesRender(artifactRef: ArtifactReferenceRenderer) {

  def toBuild(module: BuildModule): Starlark = {
    def buildArgs(name: String, dirType: String, plugins: List[Starlark], deps: List[Starlark]) =
      Map(
        "name" -> Starlark.string(name),
        "plugins" -> Starlark.list(plugins),
        "deps" -> Starlark.list(deps),
        "visibility" -> Starlark.list(List(Starlark.string("//visibility:public"))),
        "srcs" -> Starlark.function("glob", Map("include" -> Starlark.list(List(Starlark.string(s"src/$dirType/scala/**/*.scala"))))),
        "resources" -> Starlark.function("glob", Map("include" -> Starlark.list(List(Starlark.string(s"src/$dirType/resources/**/*.*")))))
      )

    def runtimeTarget = {
      val runtime = module.dependencies.filter(_.buildDef).map(x => Starlark.string(artifactRef.render(x)))
      val plugins = module.dependencies.filter(_.isPlugin).map(x => Starlark.string(artifactRef.render(x)))
      val internal = module.dependsOn.map(x => Starlark.string(s"//$x"))
      val baseArgs = buildArgs(module.name, "main", plugins, runtime ++ internal)

      module.mainClass match {
        case Some(clz) => Starlark.function("scala_image", baseArgs ++ Map("main_class" -> Starlark.string(clz)))
        case None => Starlark.function("scala_library", baseArgs)
      }
    }

    def baseTestTarget(filter: BuildDependency => Boolean, testType: String) = {
      val test = module.dependencies.filter(filter).map(x => Starlark.string(artifactRef.render(x)))

      if (test.nonEmpty) {
        val plugins = module.dependencies.filter(_.isPlugin).map(x => Starlark.string(artifactRef.render(x)))
        val internal = Set(Starlark.string(module.name))
        val baseArgs = buildArgs(s"${module.name}_$testType", testType, plugins, test ++ internal)

        Option(Starlark.function("scala_library", baseArgs))
      } else {
        Option.empty
      }
    }

    def itTarget = baseTestTarget(_.isTest, "test")

    def testTarget = baseTestTarget(_.isIntegrationTest, "it")

    //TODO: uncomment to work on test targets
    //Starlak.build(List(runtimeTarget) ++ testTarget.toList ++ itTarget.toList)
    Starlark.build(List(runtimeTarget) ++ testTarget.toList ++ itTarget.toList)
  }

  def render(directory: File, module: BuildModule): BazelArtifact = {

    val build = toBuild(module)
    val contents = scheme.cata(StarlarkAst.render).apply(build)

    BazelArtifact(directory / "BUILD", contents)
  }
}
