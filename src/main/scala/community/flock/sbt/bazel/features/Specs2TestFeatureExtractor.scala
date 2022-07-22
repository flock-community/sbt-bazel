package community.flock.sbt.bazel.features

import community.flock.sbt.bazel.core.BuildModule

object Specs2TestFeatureExtractor extends FeatureExtractor {
  override def extract(module: BuildModule): List[Feature] =
    if (module.dependencies.exists(x => x.isTest && x.groupId == "org.specs2" && x.artifactId.name.startsWith("specs2-junit"))) List(Feature.Test(TestSetup.Specs2)) else List.empty
}
