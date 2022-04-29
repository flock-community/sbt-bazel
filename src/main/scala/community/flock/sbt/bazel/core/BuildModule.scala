package community.flock.sbt.bazel.core

import java.nio.file.Path

final case class BuildModule(
  name: String,
  directory: Path,
  dependencies: List[BuildDependency] = List.empty,
  dependsOn: Set[String] = Set.empty,
  mainClass: Option[String] = None,
  testFrameworks: List[String] = Nil
)