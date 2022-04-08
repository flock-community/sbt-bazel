package community.flock.sbt.renderer

import sbt.File

final case class BazelArtifact(file: File, content: String)
