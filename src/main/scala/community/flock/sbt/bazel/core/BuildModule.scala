package community.flock.sbt.bazel.core

import java.nio.file.Path

final case class BuildProjectDependency(name: String, directory: String)

final case class BuildModule(
  name: String,
  directory: Path,
  dependencies: List[BuildDependency] = List.empty,
  mainClass: Option[String] = None,
  testFrameworks: List[String] = Nil,
  dependsOn: Set[BuildProjectDependency] = Set.empty,
  resolvers: List[BuildResolver] = Nil,
  scalacOptions: Seq[String] = List.empty
) {
  def withDependsOn(on: Set[BuildProjectDependency]): BuildModule = copy(dependsOn = on)
}