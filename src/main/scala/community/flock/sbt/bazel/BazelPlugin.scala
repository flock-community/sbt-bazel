package community.flock.sbt.bazel

import community.flock.sbt.bazel.core.{BuildArtifactId, BuildDependency, BuildDependencyConfiguration, BuildModule}
import community.flock.sbt.bazel.renderer.{BazelFormatting, JvmExternalRenderer, ScalaRulesRender}
import community.flock.sbt.bazel.starlark.{Starlark, StarlarkAst}
import higherkindness.droste.scheme
import sbt.*
import sbt.Keys.*

object BazelPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val buildRoot =
      settingKey[File]("Build root path")
  }

  import autoImport.*

  val renderer = new ScalaRulesRender(JvmExternalRenderer)

  override def buildSettings: Seq[Def.Setting[_]] = Seq(commands ++= Seq(bazelInstall))

  def bazelInstall = Command.command("bazelInstall") { s =>
    val extracted: Extracted = Project extract s
    val buildStructure = extracted.structure
    val data = buildStructure.data
    val buildUnitsMap = buildStructure.units
    val currentBuildUnit           = buildUnitsMap(extracted.currentRef.build)
    val projectsMap: Map[String, ResolvedProject] = currentBuildUnit.defined
    val projectDir = (extracted.currentRef / baseDirectory).get(data).getOrElse(sys.error("Impossible"))
    val rootDir = buildRoot.get(data) getOrElse projectDir

    val deps = buildStructure.allProjectRefs.flatMap { p =>
      for {
        name <- (p / name).get(data)
        deps <- (p / libraryDependencies).get(data)
        scalaFullVersion <- (p / scalaVersion).get(data)
        moduleDir <- (p / baseDirectory).get(data).map(f => rootDir.toPath relativize f.toPath)
        scalaBinaryVersion <- (p / scalaBinaryVersion).get(data)
        mainClass = Project.runTask((p / Keys.mainClass), s).flatMap { case (_, res) => res.toEither.toOption }
      } yield BuildModule(name, moduleDir, deps.map(d => toDependency(d, scalaFullVersion, scalaBinaryVersion)).toList, Set.empty, mainClass.flatten)
    }

    def writeDepsFile(): Unit = {
      val uniqDeps = Starlark.list(deps.flatMap(_.dependencies).toSet.map(BazelFormatting.versionedRef _ andThen Starlark.string).toList)
      val depsStr = scheme.cata(StarlarkAst.render).apply(uniqDeps)
      val depsFile = s"""DEPS = $depsStr"""

      IO.write(projectDir / "DEPS.bzl", depsFile)
    }

    def writeModules(): Unit = {
      deps.foreach { mod =>
        val artifact = renderer.render(projectDir / mod.directory.toString, mod)
        println(s"Writing file: ${artifact.file}")
        IO.write(artifact.file, artifact.content.getBytes())
      }
    }

    def writePrerequisites(): Unit = {
      IO.write(projectDir / "tools/build_rules/prelude_bazel", getClass.getResourceAsStream("/prelude.bzl").readAllBytes())
      IO.write(projectDir / "tools/build_rules/BUILD", Array.emptyByteArray)
      IO.write(projectDir / "tools/BUILD", Array.emptyByteArray)
      IO.write(projectDir / ".bazelversion", "4.2.2".getBytes())
      IO.write(projectDir / "toolchains/BUILD", getClass.getResourceAsStream("/toolchain.bzl").readAllBytes())
      IO.write(projectDir / "WORKSPACE", getClass.getResourceAsStream("/workspace.bzl").readAllBytes())
    }

    writePrerequisites()
    writeDepsFile()
    writeModules()

//    val projectsNodes = projectsMap.values map { p =>
//      DependencyGraphNode.fromResolvedProject(p, projectsMap)
//    }

    s
  }

  private def crossName(moduleId: ModuleID, scalaFullVersion: String, scalaBinaryVersion: String): Option[String] =
    CrossVersion(moduleId.crossVersion, scalaFullVersion, scalaBinaryVersion).map(_ (moduleId.name))

  private def toDependency(moduleId: ModuleID, scalaFullVersion: String, scalaBinaryVersion: String): BuildDependency = {
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
      artifactId = BuildArtifactId(moduleId.name, crossName(moduleId, scalaFullVersion, scalaBinaryVersion)),
      version = moduleId.revision,
      sbtVersion = moduleId.extraAttributes.get("e:sbtVersion"),
      scalaVersion = moduleId.extraAttributes.get("e:scalaVersion"),
      configurations = decodeConfigurations(moduleId.configurations)
    )
  }
}
