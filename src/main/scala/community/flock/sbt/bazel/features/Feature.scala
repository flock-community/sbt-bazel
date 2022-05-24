package community.flock.sbt.bazel.features

sealed trait Feature

object Feature {
  final case object Docker extends Feature
  final case object Lib extends Feature
  final case class Test(setup: TestSetup) extends Feature
}