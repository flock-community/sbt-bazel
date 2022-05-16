package community.flock.sbt.bazel

import community.flock.sbt.bazel.core.{BuildArtifactId, BuildDependency, BuildDependencyConfiguration, BuildModule, BuildProjectDependency, BuildResolver}
import community.flock.sbt.bazel.renderer.*
import community.flock.sbt.bazel.starlark.StarlarkProgram
import sbt.*

object BazelPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val buildRoot =
      settingKey[File]("Build root path")

    val bazelVersion =
      settingKey[String]("Bazel version to use")

    val bazelScalacJvmFlags =
      settingKey[List[String]]("Scala compiler JVM opts")
  }

  import autoImport.*

  val renderer = new ScalaRulesRender(JvmExternalRenderer)

  override def buildSettings: Seq[Def.Setting[_]] = Seq(Keys.commands ++= Seq(bazelInstall))

  def bazelInstall = Command.command("bazelInstall") { s =>
    val extracted: Extracted = Project extract s
    val buildStructure = extracted.structure
    val data = buildStructure.data
    val buildUnitsMap = buildStructure.units
    val currentBuildUnit           = buildUnitsMap(extracted.currentRef.build)
    val projectsMap = currentBuildUnit.defined
    val projectDir = (extracted.currentRef / Keys.baseDirectory).get(data).getOrElse(sys.error("Impossible"))
    val rootDir = (ThisBuild / buildRoot).get(data) getOrElse projectDir
    val bzlVersion = (ThisBuild / bazelVersion).get(data) getOrElse "4.2.2"
    val internalDeps = projectsMap.values.toList.map(p => (p.id, p.dependencies.flatMap(dep => projectsMap.get(dep.project.project)).map(_.id).toSet)).toMap

    val moduleMap = buildStructure.allProjectRefs.flatMap { p =>
      for {
        name <- (p / Keys.name).get(data)
        deps <- (p / Keys.libraryDependencies).get(data)
        scalaFullVersion <- (p / Keys.scalaVersion).get(data)
        moduleDir <- (p / Keys.baseDirectory).get(data).map(f => rootDir.toPath relativize f.toPath)
        scalaBinaryVersion <- (p / Keys.scalaBinaryVersion).get(data)
        mainClass = Project.runTask((p / Keys.mainClass), s).flatMap { case (_, res) => res.toEither.toOption }
        resolvers = Project.runTask((p / Keys.fullResolvers), s).flatMap { case (_, res) => res.toEither.toOption }
        scalacOpts = Project.runTask((p / Keys.scalacOptions), s).flatMap { case (_, res) => res.toEither.toOption }
        scalacCompilerOpts = (p / bazelScalacJvmFlags).get(data)
      } yield {
        val foundResolvers = resolvers.toList.flatten.collect {
          case repo: MavenRepository if !repo.root.startsWith("file:") =>
            BuildResolver.Maven(repo.name, repo.root, None)
          case repo: URLRepository =>
            val ivyPatterns = repo.patterns.ivyPatterns.mkString
            BuildResolver.Ivy(repo.name, ivyPatterns, None)
        }

        BuildModule(
          name = name,
          directory = moduleDir,
          dependencies = deps.map(d => toDependency(d, scalaFullVersion, scalaBinaryVersion)).toList,
          mainClass = mainClass.flatten,
          resolvers = foundResolvers,
          scalacOptions = scalacOpts.map(_.toList).getOrElse(List.empty),
          scalacCompilerOptions = scalacCompilerOpts.getOrElse(List.empty)
        )
      }
    }.filter(!_.name.contains("root")).map(x => x.name -> x).toMap


    val modules = moduleMap.map { case (name, mod) =>
      mod.withDependsOn(internalDeps.getOrElse(name, Set.empty).flatMap(key => moduleMap.get(key).map(x => BuildProjectDependency(key, x.directory.toString))))
    }

    def writeSetup(): Unit = {
      val dependencies = modules.flatMap(_.dependencies).toSet.map(BazelFormatting.versionedRef)
      val resolvers = modules.flatMap(_.resolvers).map(_.show).toSet

      IO.write(projectDir / "WORKSPACE", StarlarkProgram.show(WorkspaceRenderer.render(dependencies, resolvers)))
      IO.write(projectDir / "toolchains/BUILD", StarlarkProgram.show(ScalaToolchainRenderer.render))
    }

    def writeModules(): Unit = modules.foreach { mod =>
      val artifact = renderer.render(projectDir / mod.directory.toString, mod)
      IO.write(artifact.file, artifact.content.getBytes())
    }

    def writePrerequisites(): Unit = {
      IO.write(projectDir / ".bazelversion", bzlVersion.getBytes())
      IO.write(projectDir / "tools/build_rules/prelude_bazel", getClass.getResourceAsStream("/prelude.bzl").readAllBytes())
      IO.write(projectDir / "tools/build_rules/BUILD", Array.emptyByteArray)
      IO.write(projectDir / "tools/BUILD", Array.emptyByteArray)
      IO.write(projectDir / "BUILD", Array.emptyByteArray)
    }

    writePrerequisites()
    writeSetup()
    writeModules()

    s
  }

  private def crossName(moduleId: ModuleID, scalaFullVersion: String, scalaBinaryVersion: String): Option[String] =
    CrossVersion(moduleId.crossVersion, scalaFullVersion, scalaBinaryVersion).map(_ (moduleId.name))

  private def toDependency(moduleId: ModuleID, scalaFullVersion: String, scalaBinaryVersion: String): BuildDependency = {
    def decodeConfigurations(configurations: Option[String]): Set[BuildDependencyConfiguration] =
      configurations match {
        case Some(cfg) =>
          val mapping = Map(
            "compile" -> BuildDependencyConfiguration.Compile,
            "it" -> BuildDependencyConfiguration.IntegrationTest,
            "test" -> BuildDependencyConfiguration.Test,
            "plugin" -> BuildDependencyConfiguration.Plugin
          )

          mapping.foldLeft(Set.empty[BuildDependencyConfiguration]) { case (acc, (key, value)) =>
            if(cfg.contains(key)) acc + value else acc
          }

        case None => Set(BuildDependencyConfiguration.Compile)
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
