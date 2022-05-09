package community.flock.sbt.bazel

import buildinfo.BuildInfo

import java.io.File
import java.nio.file.{Files, Path, StandardOpenOption}
import scala.sys.process.*

class IntegrationSpec extends munit.FunSuite {

  def fileFromPath(path: String) = new File(getClass.getResource(path).getPath)

  def run(path: File, cmd: String): Int = Process(cmd, path).run().exitValue()

  def writePluginFile(path: File): Path = {
    val pluginsPath = path.toPath.resolve("project/plugins.sbt")
    Files.createDirectories(path.toPath.resolve("project"))
    Files.write(pluginsPath, s"""addSbtPlugin("community.flock" % "sbt-bazel" % "${BuildInfo.version}")""".getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

  def writeBazelrc(path: File): Path = {
    val bazelRcPath = path.toPath.resolve(".bazelrc")
    val contents =
      s"""build --bes_results_url=https://app.buildbuddy.io/invocation/
        |build --bes_backend=grpcs://remote.buildbuddy.io
        |build --remote_cache=grpcs://remote.buildbuddy.io
        |build --remote_header=x-buildbuddy-api-key=${System.getenv("BUILDBUDDY_API_TOKEN")}
        |build --remote_timeout=3600
        |""".stripMargin

    Files.write(bazelRcPath, contents.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

  test("should install sbt project") {
    val path = fileFromPath("/simple-zio-project")
    writePluginFile(path)
    writeBazelrc(path)
    assertEquals(run(path, "sbt bazelInstall"), 0)
    assertEquals(run(path, "bazel build //modules/api"), 0)
  }

}
