package community.flock.sbt.bazel.features


sealed trait TestSetup

object TestSetup {
  case object JUnit extends TestSetup
  case object Specs2 extends TestSetup
  case object Scalatest extends TestSetup
}