package community.flock.sbt.bazel.features

import community.flock.sbt.bazel.core.BuildModule

object MUnitTestFeatureExtractor extends FeatureExtractor {
  override def extract(module: BuildModule): List[Feature] =
    if (module.dependencies.exists(x => x.isTest && x.groupId == "org.scalameta" && x.artifactId.name == "munit")) List(Feature.Test(TestSetup.JUnit)) else List.empty
}
