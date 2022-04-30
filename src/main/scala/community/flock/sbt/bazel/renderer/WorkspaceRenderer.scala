package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.starlark.{Argument, Starlark, StarlarkProgram, StarlarkStmt}

object WorkspaceRenderer {
  private def httpArchive(props: Map[String, String]) =
    Starlark.functionNamed("http_archive", props.mapValues(Starlark.string(_).expr)).stmt

  object versions {
    val skylib = "1.0.3"
    val rulesScala = "e7a948ad1948058a7a5ddfbd9d1629d6db839933"
    val rulesJvmExternal = "4.2"
    val rulesDocker = "0.24.0"
    val scala = "2.13.8"
  }

  private val githubBazelBuildUrl = "https://github.com/bazelbuild"

  def render(dependencies: Set[String], resolvers: Set[String]): StarlarkProgram = StarlarkProgram.of(
    StarlarkStmt.Load("@bazel_tools//tools/build_defs/repo:http.bzl", List(Argument.Literal("http_archive"))),
    httpArchive(Map("name" -> "bazel_skylib", "sha256" -> "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c", "type" -> "tar.gz", "url" -> s"https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/${versions.skylib}/bazel-skylib-${versions.skylib}.tar.gz")),
    httpArchive(Map("name" -> "io_bazel_rules_scala", "sha256" -> "76e1abb8a54f61ada974e6e9af689c59fd9f0518b49be6be7a631ce9fa45f236", "type" -> "zip", "strip_prefix" -> s"rules_scala-${versions.rulesScala}", "url" -> s"$githubBazelBuildUrl/rules_scala/archive/${versions.rulesScala}.zip")),
    httpArchive(Map("name" -> "io_bazel_rules_docker", "sha256" -> "27d53c1d646fc9537a70427ad7b034734d08a9c38924cc6357cc973fed300820", "strip_prefix" -> s"rules_docker-${versions.rulesDocker}", "url" -> s"$githubBazelBuildUrl/rules_docker/releases/download/v${versions.rulesDocker}/rules_docker-v${versions.rulesDocker}.tar.gz")),
    httpArchive(Map("name" -> "rules_jvm_external", "sha256" -> "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca", "strip_prefix" -> s"rules_jvm_external-${versions.rulesJvmExternal}", "url" -> s"$githubBazelBuildUrl/rules_jvm_external/archive/${versions.rulesJvmExternal}.zip")),

    // scala rules
    StarlarkStmt.Load("@io_bazel_rules_scala//:scala_config.bzl", List(Argument.Literal("scala_config"))),
    Starlark.functionNamed("scala_config", Map("scala_version" -> Starlark.string(versions.scala).expr)).stmt,
    StarlarkStmt.Load("@io_bazel_rules_scala//scala:scala.bzl", List(Argument.Literal("scala_repositories"))),
    Starlark.functionNamed("scala_repositories", Map.empty).stmt,
    Starlark.function("register_toolchains", Argument.Literal("//toolchains:scala_toolchain")).stmt,

    // jvm external rules
    StarlarkStmt.Load("@rules_jvm_external//:repositories.bzl", List(Argument.Literal("rules_jvm_external_deps"))),
    Starlark.functionNamed("rules_jvm_external_deps", Map.empty).stmt,
    StarlarkStmt.Load("@rules_jvm_external//:setup.bzl", List(Argument.Literal("rules_jvm_external_setup"))),
    Starlark.functionNamed("rules_jvm_external_setup", Map.empty).stmt,
    StarlarkStmt.Load("@rules_jvm_external//:defs.bzl", List(Argument.Literal("maven_install"))),
    Starlark.functionNamed("maven_install", Map("artifacts" -> Starlark.list(dependencies.map(Starlark.string).toList).expr, "repositories" -> Starlark.list(resolvers.map(Starlark.string).toList).expr)).stmt,

    // docker
    StarlarkStmt.Load("@io_bazel_rules_docker//toolchains/docker:toolchain.bzl", List(Argument.Named("docker_toolchain_configure", Starlark.string("toolchain_configure").expr))),
    StarlarkStmt.Load("@io_bazel_rules_docker//repositories:repositories.bzl", List(Argument.Named("container_repositories", Starlark.string("repositories").expr))),
    Starlark.functionNamed("container_repositories", Map.empty).stmt,
    StarlarkStmt.Load("@io_bazel_rules_docker//repositories:deps.bzl", List(Argument.Named("container_deps", Starlark.string("deps").expr))),
    Starlark.functionNamed("container_deps", Map.empty).stmt,
    StarlarkStmt.Load("@io_bazel_rules_docker//container:container.bzl", List(Argument.Literal("container_pull"))),
    Starlark.functionNamed("container_pull", Map("name" -> Starlark.string("java_base").expr, "registry" -> Starlark.string("gcr.io").expr, "repository" -> Starlark.string("distroless/java").expr, "digest" -> Starlark.string("sha256:deadbeef").expr)).stmt,
    StarlarkStmt.Load("@io_bazel_rules_docker//scala:image.bzl", List(Argument.Named("_scala_image_repos", Starlark.string("repositories").expr))),
    Starlark.functionNamed("_scala_image_repos", Map.empty).stmt
  )
}
