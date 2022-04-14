package community.flock.sbt.core

final case class BuildModule(
  name: String,
  directory: Option[String],
  dependencies: List[BuildDependency] = List.empty,
  dependsOn: Set[String] = Set.empty,
  mainClass: Option[String] = None,
  testFrameworks: List[String] = Nil
)