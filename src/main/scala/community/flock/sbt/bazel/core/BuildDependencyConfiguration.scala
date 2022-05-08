package community.flock.sbt.bazel.core

sealed trait BuildDependencyConfiguration

object BuildDependencyConfiguration {
  case object Compile extends BuildDependencyConfiguration
  case object Plugin extends BuildDependencyConfiguration
  case object Test extends BuildDependencyConfiguration
  case object IntegrationTest extends BuildDependencyConfiguration
}
