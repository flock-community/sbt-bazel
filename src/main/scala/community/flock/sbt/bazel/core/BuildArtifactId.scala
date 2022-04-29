package community.flock.sbt.bazel.core

final case class BuildArtifactId(name: String, maybeCrossName: Option[String]) {
  def normalizedName: String = maybeCrossName.getOrElse(name)
}