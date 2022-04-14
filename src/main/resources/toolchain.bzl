load("@io_bazel_rules_scala//scala:scala_toolchain.bzl", "scala_toolchain")
load("@io_bazel_rules_scala//scala:providers.bzl", "declare_deps_provider")

SCALAC_OPTS = ["-deprecation",
"-explaintypes",
"-feature",
"-language:existentials",
"-language:higherKinds",
"-language:implicitConversions",
"-unchecked",
"-Xfatal-warnings",
"-Ybackend-parallelism",
"8",
"-Ypatmat-exhaust-depth",
"off",
"-Xlint:adapted-args",
"-Xlint:constant",
"-Xlint:delayedinit-select",
"-Xlint:doc-detached",
"-Xlint:inaccessible",
"-Xlint:infer-any",
"-Xlint:missing-interpolator",
"-Xlint:nullary-unit",
"-Xlint:option-implicit",
"-Xlint:package-object-classes",
"-Xlint:poly-implicit-overload",
"-Xlint:private-shadow",
"-Xlint:stars-align",
"-Xlint:type-parameter-shadow",
"-Ywarn-dead-code",
"-Ywarn-extra-implicit",
"-Ywarn-numeric-widen",
"-Ywarn-unused:implicits",
"-Ywarn-unused:imports",
"-Ywarn-unused:locals",
"-Ywarn-unused:params",
"-Ywarn-unused:patvars",
"-Ywarn-unused:privates",
"-Ywarn-value-discard",
"-Ycache-plugin-class-loader:last-modified",
"-Ycache-macro-class-loader:last-modified",
"-Ymacro-annotations",
"-Wunused"]


scala_toolchain(
    name = "scala",
    strict_deps_mode = "off",
    unused_dependency_checker_mode = "off",
    dependency_mode = "transitive",
    dependency_tracking_method = "ast",
    scalacopts = SCALAC_OPTS,
    visibility = ["//visibility:public"],
)

toolchain(
    name = "scala_toolchain",
    toolchain_type = "@io_bazel_rules_scala//scala:toolchain_type",
    toolchain = "scala",
    visibility = ["//visibility:public"]
)

