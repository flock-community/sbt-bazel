package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.core.BuildDependency

object BazelDepsRenderer extends ArtifactReferenceRenderer {
  override def render(artifact: BuildDependency): String =
    "@jvm_deps//:" + BazelFormatting.formatString(artifact.groupId) + "_" + BazelFormatting.artifactId(artifact.artifactId, artifact.scalaVersion, BazelFormatting.formatString)
}
