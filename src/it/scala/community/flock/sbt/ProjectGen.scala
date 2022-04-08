package community.flock.sbt

import java.nio.file.{Files, Path, StandardOpenOption}

object Util {
  def ensureDir(path: Path): Unit = {
    val parentDir = path.getParent
    if (!Files.exists(parentDir)) Files.createDirectories(parentDir) else ()
  }
}

final case class Dependency(groupId: String, artifactId: String, version: String, scope: Option[String] = None) {
  def sbtDef: String = {
    scope match {
      case Some(value) => s""""$groupId" % "$artifactId" % "$version" % "$value""""
      case None => s""""$groupId" % "$artifactId" % "$version""""
    }
  }
}
final case class SourceFile(relativeLocation: Path, contents: String) {
  def writeFiles(to: Path) = {
    Util.ensureDir(to.resolve(relativeLocation))
    Files.write(to.resolve(relativeLocation), contents.getBytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }
}
final case class Module private (
                                  name: String,
                                  dependencies: List[Dependency] = List.empty,
                                  source: List[SourceFile] = List.empty,
                                  dependsOn: Set[String] = Set.empty,
                                  mainClass: Option[String] = None,
                                  testFrameworks: List[String] = Nil,
                                  scalaVersion: String
                                ) {
  def withSource(sourceFiles: SourceFile*): Module =
    copy(source = source ++ sourceFiles.toList)

  def withDependsOn(depends: String*): Module =
    copy(dependsOn = dependsOn ++ depends.toSet)

  def withMainClass(mainClass: String): Module =
    copy(mainClass = Some(mainClass))

  def withScalaversion(version: String): Module =
    copy(scalaVersion = version)

  def withTestFramework(framework: String*): Module =
    copy(testFrameworks = testFrameworks ++ framework.toList)

  def withDependency(dependency: Dependency*): Module =
    copy(dependencies = dependencies ++ dependency.toList)

  def sbtDef: String = {
    val modulePath = s"modules/$name"
    val sb = new StringBuilder()

    sb.append(s"""lazy val $name = project.in(file("$modulePath"))\n""")
    sb.append(s"""\t.settings(scalaVersion := "${scalaVersion}")\n""")


    if(dependencies.nonEmpty) {
      sb.append(s"""\t.settings(libraryDependencies ++= Seq(${dependencies.map(_.sbtDef).mkString(",")}))\n""")
    }

    mainClass.foreach(mainClz => sb.append(s"""\t.settings(mainClass := Some("$mainClz"))\n"""))

    if(dependsOn.nonEmpty) {
      sb.append(s"""\t.dependsOn(${dependsOn.mkString(", ")})\n""")
    }

    if(testFrameworks.nonEmpty) {
      testFrameworks.foreach(framework => sb.append(s"""\t.settings(testFrameworks += new TestFramework("$framework"))\n"""))
    }

    sb.toString()
  }
}

object Module {
  def apply(name: String): Module = Module(name, scalaVersion = "2.13.8")
}

final case class Build(modules: List[Module], sbtPlugins: List[String]) {
  def sbtDef = modules.map(_.sbtDef).mkString("\r\n\r\n")
  def writeFiles(to: Path): Unit = {
    Util.ensureDir(to)
    modules.foreach(_.source.foreach(_.writeFiles(to)))

    val moduleNames = modules.map(_.name)
    val root = s"""lazy val root = project.in(file(".")).aggregate(${moduleNames.mkString(", ")})"""
    val buildSbt = sbtDef + "\n" + root

    Util.ensureDir(to.resolve("project/plugins.sbt"))
    Files.write(to.resolve("project/plugins.sbt"), sbtPlugins.mkString("\r\n").getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
    Files.write(to.resolve("build.sbt"), buildSbt.getBytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }
}


