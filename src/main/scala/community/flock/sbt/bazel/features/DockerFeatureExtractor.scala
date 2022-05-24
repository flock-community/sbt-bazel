package community.flock.sbt.bazel.features

import community.flock.sbt.bazel.core.BuildModule

object DockerFeatureExtractor extends FeatureExtractor {
  override def extract(build: BuildModule): List[Feature] =
    if (build.mainClass.isEmpty) List.empty else List(Feature.Docker)
}
