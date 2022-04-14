package community.flock.sbt.renderer

import community.flock.sbt.core._

trait ArtifactReferenceRenderer {
  def render(artifact: BuildDependency): String
}
