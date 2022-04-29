package community.flock.sbt.bazel.core

import java.nio.file.Path

final case class BuildModule(
  name: String,
  directory: Path,
  dependencies: List[BuildDependency] = List.empty,
  mainClass: Option[String] = None,
  testFrameworks: List[String] = Nil,
  dependsOn: Set[String] = Set.empty
) {
  def withDependsOn(on: Set[String]) = copy(dependsOn = on)
}