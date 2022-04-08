ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-bazel",
    organization := "community.flock",
    sbtPlugin := true,
    publishMavenStyle := true,
    publishTo := Some("Artifactory Realm" at "https://flock.jfrog.io/artifactory/sbt-bazel;build.timestamp=" + new java.util.Date().getTime),
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
    libraryDependencies ++= Seq("io.kevinlee" %% "just-semver" % "0.3.0")
  )
