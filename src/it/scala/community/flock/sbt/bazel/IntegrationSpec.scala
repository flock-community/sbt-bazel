package community.flock.sbt.bazel

import buildinfo.BuildInfo

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.sys.process.*

class IntegrationSpec extends munit.FunSuite {

  test("should install sbt project") {
    val path = fileFromPath("/simple-zio-project")
    writePluginFile(path)
    writeBazelrc(path)
    assertEquals(run(path, "sbt bazelInstall"), 0)
    assertEquals(run(path, "bazel build //modules/api"), 0)
  }

  private def fileFromPath(path: String): Path =
    Paths.get(getClass.getResource(path).getPath)

  private def run(path: Path, cmd: String): Int =
    Process(cmd, path.toFile).run().exitValue()

  private def writePluginFile(path: Path): Path = {
    val pluginsPath = path.resolve("project/plugins.sbt")
    Files.createDirectories(path.resolve("project"))
    Files.write(pluginsPath, s"""addSbtPlugin("community.flock" % "sbt-bazel" % "${BuildInfo.version}")""".getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

  private def writeBazelrc(path: Path): Path = {
    val bazelRcPath = path.resolve(".bazelrc")
    val local = "build --incompatible_java_common_parameters=false"
    val remote =
      s"""build --incompatible_java_common_parameters=false
         |
         |build --bes_results_url=https://app.buildbuddy.io/invocation/
         |build --bes_backend=grpcs://remote.buildbuddy.io
         |build --remote_cache=grpcs://remote.buildbuddy.io
         |build --remote_header=x-buildbuddy-api-key=${System.getenv("BUILDBUDDY_API_KEY")}
         |build --remote_timeout=3600
         |""".stripMargin

    val contents = if(System.getenv("BUILDBUDDY_API_KEY") == null) local else remote

    Files.write(bazelRcPath, contents.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }

}
