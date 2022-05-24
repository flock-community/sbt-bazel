package community.flock.sbt.bazel.features

import community.flock.sbt.bazel.core.BuildModule

object ScalatestTestFeatureExtractor extends FeatureExtractor {
  override def extract(module: BuildModule): List[Feature] =
    if (module.dependencies.exists(x => x.isTest && x.groupId == "org.scalatest" && x.artifactId.name == "scalatest")) List(Feature.Test(TestSetup.Scalatest)) else List.empty
}
