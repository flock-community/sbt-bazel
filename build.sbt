ThisBuild / version := "0.1.1-SNAPSHOT"

ThisBuild / publishTo := Some(
  "Artifactory Realm" at "https://flock.jfrog.io/artifactory/sbt-bazel"
)
ThisBuild / credentials += (if (sys.env.get("CI").isDefined) {
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
                            })

lazy val root = (project in file("."))
  .settings(
    name := "sbt-bazel",
    organization := "community.flock",
    sbtPlugin := true,
    publishMavenStyle := true,
    publish / skip := false,
    publishLocal / skip := false,
    libraryDependencies ++= Seq("io.kevinlee" %% "just-semver" % "0.3.0")
  )
