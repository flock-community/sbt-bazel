package community.flock.sbt.bazel.features

import community.flock.sbt.bazel.core.BuildModule

trait FeatureExtractor {
  self =>
  def extract(build: BuildModule): List[Feature]

  def ++(other: FeatureExtractor): FeatureExtractor = new FeatureExtractor {
    override def extract(build: BuildModule): List[Feature] = self.extract(build) ++ other.extract(build)
  }
}
