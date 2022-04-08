ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-bazel",
    organization := "community.flock",
    sbtPlugin := true,
    publishMavenStyle := true,
    publish / skip := false,
    publishLocal / skip := false,
    libraryDependencies ++= Seq("io.kevinlee" %% "just-semver" % "0.3.0")
    publishTo := Some("Artifactory Realm" at "https://flock.jfrog.io/artifactory/sbt-bazel"),
    if(sys.env.get("CI").isDefined){
      credentials += Credentials("Artifactory Realm", "flock.jfrog.io", "github", sys.env("JFROG_TOKEN"))
    }else{
      credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
    }
  )
