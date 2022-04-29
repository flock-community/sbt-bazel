package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.core.BuildDependency

trait ArtifactReferenceRenderer {
  def render(artifact: BuildDependency): String
}
