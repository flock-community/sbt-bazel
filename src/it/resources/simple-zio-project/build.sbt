lazy val api = project.in(file("modules/api"))
	.settings(scalaVersion := "2.13.8")
	.settings(mainClass := Some("company.Main"))
	.dependsOn(services)


lazy val services = project.in(file("modules/services"))
	.settings(scalaVersion := "2.13.8")
	.settings(libraryDependencies ++= Seq("dev.zio" % "zio_2.13" % "1.0.13","dev.zio" % "zio-test_2.13" % "1.0.13" % "test","dev.zio" % "zio-test-sbt_2.13" % "1.0.13" % "test","dev.zio" % "zio-test-magnolia_2.13" % "1.0.13" % "test"))
	.settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val root = project.in(file(".")).aggregate(api, services)