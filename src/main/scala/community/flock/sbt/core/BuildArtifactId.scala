package community.flock.sbt.core

final case class BuildArtifactId(name: String, maybeCrossName: Option[String]) {
  def normalizedName: String = maybeCrossName.getOrElse(name)
}