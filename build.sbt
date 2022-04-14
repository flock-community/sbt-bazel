ThisBuild / version := "0.1.1-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(Defaults.itSettings)
  .configs(IntegrationTest)
  .settings(
    name := "sbt-bazel",
    organization := "community.flock",
    sbtPlugin := true,
    publishMavenStyle := true,
    publishTo := Some("Artifactory Realm" at "https://flock.jfrog.io/artifactory/flock-sbt"),
    credentials += (if (sys.env.get("CI").isDefined) {
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
        "org.typelevel" %% "cats-core" % "2.7.0",
        "io.higherkindness" %% "droste-core" % "0.8.0",
        "org.scalameta" %% "munit" % "0.7.29" % "it,test"
    )
  )
