import just.semver.SemVer

import java.nio.file.Path

object ProjectGenTest extends App {

  val main = SourceFile(
    Path.of("modules/api/src/main/scala/company/Main.scala"),
    """
      |package company
      |
      |import zio._
      |
      |object Main extends App {
      |   def run(args: List[String]) = Service.sayHello.exitCode
      |}
      |""".stripMargin
  )

  val service = SourceFile(
    Path.of("modules/services/src/main/scala/company/Service.scala"),
    """
      |package company
      |
      |import zio.console._
      |
      |object Service {
      |   def sayHello = putStrLn("Hello world")
      |}
      |""".stripMargin
  )

  val serviceTest = SourceFile(
    Path.of("modules/services/src/test/scala/company/ServiceTest.scala"),
    """
      |package company
      |
      |import zio.test._
      |import zio.test.environment._
      |
      |object ServiceTest extends DefaultRunnableSpec {
      |   def spec = suite("HelloWorldSpec")(
      |    testM("sayHello correctly displays output") {
      |      for {
      |        _      <- Service.sayHello
      |        output <- TestConsole.output
      |      } yield assert(output)(Assertion.equalTo(Vector("Hello world\n")))
      |    }
      |  )
      |}
      |""".stripMargin
  )

  private val zioVersion = "1.0.13"
  private val scalaVersion = SemVer.parseUnsafe("2.13.8")
  private val scalaV = s"${scalaVersion.major.major}.${scalaVersion.minor.minor}"

  val build = Build(
    modules = List(
      Module("api")
        .withScalaversion(scalaVersion)
        .withMainClass("company.Main")
        .withDependsOn("services")
        .withSource(main)
      ,
      Module("services")
        .withScalaversion(scalaVersion)
        .withSource(service, serviceTest)
        .withDependency(
          Dependency("dev.zio", "zio_" + scalaV, zioVersion),
          Dependency("dev.zio", "zio-test_" + scalaV, zioVersion, Some("test")),
          Dependency("dev.zio", "zio-test-sbt_" + scalaV, zioVersion, Some("test")),
          Dependency("dev.zio", "zio-test-magnolia_" + scalaV, zioVersion, Some("test"))
        )
        .withTestFramework("zio.test.sbt.ZTestFramework")
    )
  )

  build.writeFiles(Path.of("/tmp/zio-test"))


}
