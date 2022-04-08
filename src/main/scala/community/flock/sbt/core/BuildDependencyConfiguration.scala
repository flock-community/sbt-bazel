package community.flock.sbt.core

sealed trait BuildDependencyConfiguration

object BuildDependencyConfiguration {
  case object Plugin extends BuildDependencyConfiguration
  case object Test extends BuildDependencyConfiguration
  case object IntegrationTest extends BuildDependencyConfiguration
}
