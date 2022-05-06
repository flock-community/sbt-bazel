ThisBuild / version := "0.1.2-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(Defaults.itSettings)
  .configs(IntegrationTest)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq(version),
    name := "sbt-bazel",
    organization := "community.flock",
    sbtPlugin := true,
    publishMavenStyle := true,
    publishTo := Some("Artifactory Realm" at "https://flock.jfrog.io/artifactory/flock-sbt"),
    credentials += (if (sys.env.contains("CI")) {
      Credentials(
        "Artifactory Realm",
        "flock.jfrog.io",
        "github",
        sys.env("JFROG_TOKEN")
      )
    } else {
        Credentials(
            Path.userHome / ".sbt" / ".credentials"
        )
    }),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
    libraryDependencies ++= Seq(
        "org.scalameta" %% "munit" % "0.7.29" % "it,test"
    )
  )
