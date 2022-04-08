package community.flock.sbt.renderer

import community.flock.sbt.core.{BuildDependency, BuildModule}
import community.flock.sbt.starlak.{Starlak, StarlakAst}
import higherkindness.droste.scheme
import sbt.*

final class ScalaRulesRender(artifactRef: ArtifactReferenceRenderer) {

  def toBuild(module: BuildModule): Starlak = {
    def buildArgs(name: String, dirType: String, plugins: List[Starlak], deps: List[Starlak]) =
      Map(
        "name" -> Starlak.string(name),
        "plugins" -> Starlak.list(plugins),
        "deps" -> Starlak.list(deps),
        "visibility" -> Starlak.list(List(Starlak.string("//visibility:public"))),
        "srcs" -> Starlak.function("glob", Map("include" -> Starlak.list(List(Starlak.string(s"src/$dirType/scala/**/*.scala"))))),
        "resources" -> Starlak.function("glob", Map("include" -> Starlak.list(List(Starlak.string(s"src/$dirType/resources/**/*.*")))))
      )

    def runtimeTarget = {
      val runtime = module.dependencies.filter(_.buildDef).map(x => Starlak.string(artifactRef.render(x)))
      val plugins = module.dependencies.filter(_.isPlugin).map(x => Starlak.string(artifactRef.render(x)))
      //TODO: the reference to internal dependency is now hardcoded to `//modules/$name`, make dynamic to the actual directory of the module
      val internal = module.dependsOn.map(x => Starlak.string(s"//modules/$x"))
      val baseArgs = buildArgs(module.name, "main", plugins, runtime ++ internal)

      module.mainClass match {
        case Some(clz) => Starlak.function("scala_image", baseArgs ++ Map("main_class" -> Starlak.string(clz)))
        case None => Starlak.function("scala_library", baseArgs)
      }
    }

    def baseTestTarget(filter: BuildDependency => Boolean, testType: String) = {
      val test = module.dependencies.filter(filter).map(x => Starlak.string(artifactRef.render(x)))

      if (test.nonEmpty) {
        val plugins = module.dependencies.filter(_.isPlugin).map(x => Starlak.string(artifactRef.render(x)))
        val internal = Set(Starlak.string(module.name))
        val baseArgs = buildArgs(s"${module.name}_$testType", testType, plugins, test ++ internal)

        Option(Starlak.function("scala_library", baseArgs))
      } else {
        Option.empty
      }
    }

    def itTarget = baseTestTarget(_.isTest, "test")

    def testTarget = baseTestTarget(_.isIntegrationTest, "it")

    //TODO: uncomment to work on test targets
    //Starlak.build(List(runtimeTarget) ++ testTarget.toList ++ itTarget.toList)
    Starlak.build(List(runtimeTarget) ++ testTarget.toList ++ itTarget.toList)
  }

  def render(directory: File, module: BuildModule): BazelArtifact = {

    val build = toBuild(module)
    val contents = scheme.cata(StarlakAst.render).apply(build)

    BazelArtifact(directory / "BUILD", contents)
  }
}
