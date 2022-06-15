package community.flock.sbt.bazel.renderer

import community.flock.sbt.bazel.starlark.{Argument, Starlark, StarlarkProgram, StarlarkStmt}

final case class BazelRule(name: String, version: String, sha256: String, url: String, archiveType: Option[String] = None, stripPrefix: Option[String] = None)

object WorkspaceRenderer {

  object rules {
    private val skylib = BazelRule("skylib", "1.0.3", "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c", "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/%1$s/bazel-skylib-%1$s.tar.gz", Some("tar.gz"))
    private val scala = BazelRule("io_bazel_rules_scala", "e7a948ad1948058a7a5ddfbd9d1629d6db839933", "76e1abb8a54f61ada974e6e9af689c59fd9f0518b49be6be7a631ce9fa45f236", "https://github.com/bazelbuild/rules_scala/archive/%1$s.zip", Some("zip"), Some("rules_scala-%1$s"))
    private val jvmExternal = BazelRule("rules_jvm_external", "4.2", "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca", "https://github.com/bazelbuild/rules_jvm_external/archive/%1$s.zip", stripPrefix = Some("rules_jvm_external-%1$s"))
    private val docker = BazelRule("io_bazel_rules_docker", "0.22.0", "59536e6ae64359b716ba9c46c39183403b01eabfbd57578e84398b4829ca499a", "https://github.com/bazelbuild/rules_docker/releases/download/v%1$s/rules_docker-v%1$s.tar.gz", stripPrefix = Some("rules_docker-%1$s"))
    private val go = BazelRule("io_bazel_rules_go", "0.32.0", "ab21448cef298740765f33a7f5acee0607203e4ea321219f2a4c85a6e0fb0a27", "https://github.com/bazelbuild/rules_go/releases/download/v%1$s/rules_go-v%1$s.zip")
    private val gazelle = BazelRule("bazel_gazelle", "0.25.0", "5982e5463f171da99e3bdaeff8c0f48283a7a5f396ec5282910b9e8a49c0dd7e", "https://github.com/bazelbuild/bazel-gazelle/releases/download/v%1$s/bazel-gazelle-v%1$s.tar.gz")

    val all = List(skylib, go, gazelle, scala, jvmExternal, docker)
  }

  def render(dependencies: Set[String], resolvers: Set[String], scalaV: String = "2.13.8"): StarlarkProgram = {
    val httpLoad = StarlarkStmt.Load("@bazel_tools//tools/build_defs/repo:http.bzl", List(Argument.Literal("http_archive")))
    val rulesImport = rules.all.map(httpArchiveRule)
    val initStatements = List(
      // go rules
      StarlarkStmt.Load("@io_bazel_rules_go//go:deps.bzl", List(Argument.Literal("go_register_toolchains"), Argument.Literal("go_rules_dependencies"))),
      Starlark.functionNamed("go_rules_dependencies", Map.empty).stmt,
      Starlark.functionNamed("go_register_toolchains", Map("version" -> Starlark.string("1.18.2").expr)).stmt,

      // gazelle rules
      StarlarkStmt.Load("@bazel_gazelle//:deps.bzl", List(Argument.Literal("go_repository"))),

      // scala rules
      StarlarkStmt.Load("@io_bazel_rules_scala//:scala_config.bzl", List(Argument.Literal("scala_config"))),
      Starlark.functionNamed("scala_config", Map("scala_version" -> Starlark.string(scalaV).expr)).stmt,
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

    StarlarkProgram.of(httpLoad +: (rulesImport ++ initStatements))
  }


  private def httpArchiveRule(rule: BazelRule) = {
    val pairs = List("name" -> rule.name, "sha256" -> rule.sha256, "url" -> rule.url.format(rule.version)) ++
      rule.stripPrefix.map("strip_prefix" -> _.format(rule.version)).toList ++
      rule.archiveType.map("type" -> _).toList

    Starlark.functionNamed("http_archive", pairs.toMap.mapValues(Starlark.string(_).expr)).stmt
  }

}
