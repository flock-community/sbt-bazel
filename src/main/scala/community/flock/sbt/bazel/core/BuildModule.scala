package community.flock.sbt.bazel.core

import community.flock.sbt.bazel.features.Feature

import java.nio.file.Path

final case class BuildProjectDependency(name: String, directory: String)

final case class BuildModule(
  name: String,
  directory: Path,
  dependencies: List[BuildDependency] = List.empty,
  mainClass: Option[String] = None,
  testFrameworks: List[String] = Nil,
  dependsOn: Set[BuildProjectDependency] = Set.empty,
  resolvers: List[BuildResolver] = Nil,
  scalacOptions: List[String] = List.empty,
  scalacCompilerOptions: List[String] = List.empty,
  features: List[Feature] = List(Feature.Lib)
) {
  def withDependsOn(on: Set[BuildProjectDependency]): BuildModule = copy(dependsOn = on)
  def withFeatures(newFeatures: List[Feature]): BuildModule = copy(features = features ++ newFeatures)
}