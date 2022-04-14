package community.flock.sbt.core

final case class BuildDependency(
  groupId: String,
  artifactId: BuildArtifactId,
  version: String,
  sbtVersion: Option[String] = None,
  scalaVersion: Option[String] = None,
  configurations: List[BuildDependencyConfiguration] = Nil
) {
  def isTest: Boolean =
    configurations.contains(BuildDependencyConfiguration.Test)

  def isIntegrationTest: Boolean =
    configurations.contains(BuildDependencyConfiguration.IntegrationTest)

  def isPlugin: Boolean =
    configurations.contains(BuildDependencyConfiguration.Plugin)

  def buildDef: Boolean =
    configurations.isEmpty
}
