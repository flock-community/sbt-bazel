package community.flock.sbt

import community.flock.sbt.core.*
import community.flock.sbt.renderer.{BazelFormatting, JvmExternalRenderer, ScalaRulesRender}
import community.flock.sbt.starlak.{Starlak, StarlakAst}
import higherkindness.droste.scheme
import sbt.*
import sbt.Keys.*

object BazelPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val bazelSetup =
      taskKey[Unit]("Sets up a bazel workspace, toolchain and such")
    val bazelModules =
      taskKey[Unit]("Install Bazel BUILD fle, introspects project settings and exports to BUILD per module")
    val bazelDeps =
      taskKey[Unit]("Install DEPS.bzl")
  }

  import autoImport.*

  val renderer = new ScalaRulesRender(JvmExternalRenderer)

  private val aggregateFilter = ScopeFilter(
    inAggregates(ThisProject),
    inConfigurations(Compile)
  )

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    bazelSetup / aggregate := false,
    bazelSetup := {
      IO.write(baseDirectory.value / "tools/build_rules/prelude_bazel", getClass.getResourceAsStream("/prelude.bzl").readAllBytes())
      IO.write(baseDirectory.value / "tools/build_rules/BUILD", Array.emptyByteArray)
      IO.write(baseDirectory.value / "tools/BUILD", Array.emptyByteArray)
      IO.write(baseDirectory.value / ".bazelversion", "4.2.2".getBytes())
      IO.write(baseDirectory.value / "toolchains/BUILD", getClass.getResourceAsStream("/toolchain.bzl").readAllBytes())
      IO.write(baseDirectory.value / "WORKSPACE", getClass.getResourceAsStream("/workspace.bzl").readAllBytes())
    },
    bazelDeps / aggregate := false,
    bazelDeps := {
      val dirs = baseDirectory.?.all(aggregateFilter).value.flatten
      val baseDir = baseDirectory.value
      val deps =
        Starlak.list(dirs.flatMap { module => IO.read(module / "target/bazel/deps").split("\r\n") }.toList.map(Starlak.string))

      val depsStr = scheme.cata(StarlakAst.render).apply(deps)

      val depsFile = s"""DEPS = $depsStr"""

      IO.write(baseDir / "DEPS.bzl", depsFile)
    },
    bazelModules := {
      val scalaBinaryVersionValue = scalaBinaryVersion.value
      val scalaVersionValue = scalaVersion.value
      val dependencies = libraryDependencies.value.map(moduleId => toDependency(moduleId, scalaVersionValue, scalaBinaryVersionValue)).toList
      val basePath = Keys.rootPaths.value.get("BASE")
      val module = BuildModule(
        name = Keys.name.value.toLowerCase(),
        directory = basePath.map(p => p.relativize(baseDirectory.value.toPath).toString).filter(_.nonEmpty),
        dependencies = dependencies,
        dependsOn = projectDependencies.value.map(_.name).toSet,
        mainClass = (Compile / mainClass).value,
        testFrameworks = (Compile / testFrameworks).value.flatMap(_.implClassNames).toList
      )

      val artifact = renderer.render(baseDirectory.value, module)

      val exportedDeps =  dependencies.map(BazelFormatting.versionedRef).sorted.mkString("\r\n")

      IO.write(target.value / "bazel/deps", exportedDeps)

      IO.write(artifact.file, artifact.content.getBytes())
    }
  )

  private def crossName(moduleId: ModuleID, scalaVersion: String, scalaBinaryVersion: String): Option[String] =
    CrossVersion(moduleId.crossVersion, scalaVersion, scalaBinaryVersion).map(_ (moduleId.name))

  private def toDependency(moduleId: ModuleID, scalaVersion: String, scalaBinaryVersion: String): BuildDependency = {
    def decodeConfigurations(configurations: Option[String]): List[BuildDependencyConfiguration] =
      configurations match {
        case Some(cfg) =>
          cfg.toLowerCase.split(',').toList.collect {
            case "it" => BuildDependencyConfiguration.IntegrationTest
            case "test" => BuildDependencyConfiguration.Test
            case "plugin" => BuildDependencyConfiguration.Plugin
          }
        case None => Nil
      }

    BuildDependency(
      groupId = moduleId.organization,
      artifactId = BuildArtifactId(moduleId.name, crossName(moduleId, scalaVersion, scalaBinaryVersion)),
      version = moduleId.revision,
      sbtVersion = moduleId.extraAttributes.get("e:sbtVersion"),
      scalaVersion = moduleId.extraAttributes.get("e:scalaVersion"),
      configurations = decodeConfigurations(moduleId.configurations)
    )
  }
}
