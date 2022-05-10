package community.flock.sbt.bazel

import buildinfo.BuildInfo

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.sys.process.*

class IntegrationSpec extends munit.FunSuite {

  test("simple zio project") {
    val path = fileFromPath("/simple-zio-project")
    writePluginFile(path)
    writeBazelrc(path)
    assertEquals(run(path, "sbt bazelInstall"), 0)
    assertEquals(run(path, "bazel build //modules/api"), 0)
  }

  test("play project") {
    val path = fileFromPath("/play-project")
    writePluginFile(path, List("""addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.15")"""))
    writeBazelrc(path)
    assertEquals(run(path, "sbt bazelInstall"), 0)
    assertEquals(run(path, "bazel build //:play-scala-seed"), 0)
  }

  private def fileFromPath(path: String): Path =
    Paths.get(getClass.getResource(path).getPath)

  private def run(path: Path, cmd: String): Int =
    Process(cmd, path.toFile).run().exitValue()

  private def writePluginFile(path: Path, plugins: List[String] = Nil): Path = {
    val pluginsPath = path.resolve("project/plugins.sbt")
    val lines = List(s"""addSbtPlugin("community.flock" % "sbt-bazel" % "${BuildInfo.version}")""") ++ plugins
    Files.createDirectories(path.resolve("project"))
    Files.write(pluginsPath, lines.mkString("\r\n").getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

  private def writeBazelrc(path: Path): Path = {
    val bazelRcPath = path.resolve(".bazelrc")
    val contents =
      s"""build --bes_results_url=https://app.buildbuddy.io/invocation/
         |build --bes_backend=grpcs://remote.buildbuddy.io
         |build --remote_cache=grpcs://remote.buildbuddy.io
         |build --remote_header=x-buildbuddy-api-key=${System.getenv("BUILDBUDDY_API_KEY")}
         |build --remote_timeout=3600
         |""".stripMargin

    Files.write(bazelRcPath, contents.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

}
