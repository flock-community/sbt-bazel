name := """play-scala-seed"""
organization := "flock.community"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "flock.community.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "flock.community.binders._"
