package community.flock.sbt.renderer

import community.flock.sbt.core.BuildDependency

object BazelDepsRenderer extends ArtifactReferenceRenderer {
  override def render(artifact: BuildDependency): String =
    "@jvm_deps//:" + BazelFormatting.formatString(artifact.groupId) + "_" + BazelFormatting.artifactId(artifact.artifactId, artifact.scalaVersion, BazelFormatting.formatString)
}
