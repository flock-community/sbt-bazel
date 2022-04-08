ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-bazel-gen",
    libraryDependencies ++= Seq("io.kevinlee" %% "just-semver" % "0.3.0")
  )
