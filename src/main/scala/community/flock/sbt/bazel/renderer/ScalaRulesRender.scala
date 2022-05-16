package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.core.{BuildDependency, BuildModule}
import community.flock.sbt.bazel.starlark.{Starlark, StarlarkExpr, StarlarkPrimitive, StarlarkProgram}
import sbt.*

final class ScalaRulesRender(artifactRef: ArtifactReferenceRenderer) {

  def toBuild(module: BuildModule) = {
    def buildArgs(name: String, dirType: String, plugins: Set[String], deps: Set[String]): Map[String, StarlarkExpr] =
      Map(
        "name" -> Starlark.string(name).expr,
        "plugins" -> Starlark.list(plugins.toList.map(Starlark.string)).expr,
        "deps" -> Starlark.list(deps.toList.map(Starlark.string)).expr,
        "visibility" -> Starlark.list(List(Starlark.string("//visibility:public"))).expr,
        "scalacopts" -> Starlark.list(module.scalacOptions.map(Starlark.string)).expr,
        "scalac_jvm_flags" -> Starlark.list(module.scalacCompilerOptions.map(Starlark.string)).expr,
        "srcs" -> Starlark.functionNamed("glob", Map("include" -> Starlark.list(List(Starlark.string(s"src/$dirType/scala/**/*.scala"))).expr)),
        "resources" -> Starlark.functionNamed("glob", Map("include" -> Starlark.list(List(Starlark.string(s"src/$dirType/resources/**/*.*"))).expr))
      )

    def runtimeTarget = {
      val runtime = module.dependencies.filter(_.isCompile).toSet.map(artifactRef.render)
      val plugins = module.dependencies.filter(_.isPlugin).toSet.map(artifactRef.render)
      val internal = module.dependsOn.map(x => s"//${x.directory}:${x.name}")
      val baseArgs = buildArgs(module.name, "main", plugins, runtime ++ internal)

      module.mainClass match {
        case Some(clz) => Starlark.functionNamed("scala_image", baseArgs ++ Map("main_class" -> Starlark.string(clz).expr)).stmt
        case None => Starlark.functionNamed("scala_library", baseArgs).stmt
      }
    }

    def baseTestTarget(filter: BuildDependency => Boolean, testType: String) = {
      val test = module.dependencies.filter(filter).toSet.map(artifactRef.render)

      if (test.nonEmpty) {
        val plugins = module.dependencies.filter(_.isPlugin).toSet.map(artifactRef.render)
        val internal = Set(s":${module.name}")
        val baseArgs = buildArgs(s"${module.name}_$testType", testType, plugins, test ++ internal)

        Option(Starlark.functionNamed("scala_library", baseArgs).stmt)
      } else {
        Option.empty
      }
    }

    def itTarget = baseTestTarget(_.isTest, "test")

    def testTarget = baseTestTarget(_.isIntegrationTest, "it")

    StarlarkProgram(List(runtimeTarget) ++ testTarget.toList ++ itTarget.toList)
  }

  def render(directory: File, module: BuildModule): BazelArtifact = {

    val build = toBuild(module)
    val contents = StarlarkProgram.show(build)

    BazelArtifact(directory / "BUILD", contents)
  }
}
