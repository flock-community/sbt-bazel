package community.flock.sbt.bazel.core

final case class BuildDependency(
  groupId: String,
  artifactId: BuildArtifactId,
  version: String,
  sbtVersion: Option[String] = None,
  scalaVersion: Option[String] = None,
  configurations: Set[BuildDependencyConfiguration] = Set.empty
) {
  def isCompile: Boolean =
    configurations.contains(BuildDependencyConfiguration.Compile)

  def isTest: Boolean =
    configurations.contains(BuildDependencyConfiguration.Test)

  def isIntegrationTest: Boolean =
    configurations.contains(BuildDependencyConfiguration.IntegrationTest)

  def isPlugin: Boolean =
    configurations.contains(BuildDependencyConfiguration.Plugin)
}
