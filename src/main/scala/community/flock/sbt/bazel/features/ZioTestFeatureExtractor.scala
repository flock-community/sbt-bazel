package community.flock.sbt.bazel.features

import community.flock.sbt.bazel.core.BuildModule

object ZioTestFeatureExtractor extends FeatureExtractor {
  override def extract(module: BuildModule): List[Feature] =
    if (module.dependencies.exists(x => x.isTest && x.groupId == "dev.zio" && x.artifactId.name.startsWith("zio-test-junit"))) List(Feature.Test(TestSetup.JUnit)) else List.empty
}
