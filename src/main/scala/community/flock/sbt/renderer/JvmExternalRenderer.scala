package community.flock.sbt.renderer

import community.flock.sbt.core.BuildDependency

object JvmExternalRenderer extends ArtifactReferenceRenderer {
  override def render(artifact: BuildDependency): String =
    "@maven//:" + BazelFormatting.formatString(artifact.groupId) + "_" + BazelFormatting.artifactId(artifact.artifactId, artifact.scalaVersion, BazelFormatting.formatString)
}
