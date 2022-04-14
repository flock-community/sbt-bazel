package community.flock.sbt.renderer

import community.flock.sbt.core.{BuildArtifactId, BuildDependency}

object BazelFormatting {
  def formatString(str: String): String =
    str
      .replace(".", "_")
      .replace("-", "_")

  def artifactId(artifactId: BuildArtifactId, scalaVersion: Option[String], transform: String => String): String = scalaVersion match {
    case Some(v) => transform(artifactId.normalizedName) + "_" + transform(v)
    case None => transform(artifactId.normalizedName)
  }

  def versionedRef(dep: BuildDependency): String =
    dep.groupId + ":" + artifactId(dep.artifactId, dep.scalaVersion, identity) + ":" + dep.version

}
