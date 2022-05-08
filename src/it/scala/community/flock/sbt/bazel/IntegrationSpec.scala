package community.flock.sbt.bazel

import buildinfo.BuildInfo

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.sys.process.*

class IntegrationSpec extends munit.FunSuite {

  def fileFromPath(path: String) = new File(getClass.getResource(path).getPath)

  def run(path: File, cmd: String): Int = {
    Process(cmd, path).run().exitValue()
  }

  def writePluginFile(path: File): Path =
    Files.write(path.toPath.resolve("project/plugins.sbt"), s"""addSbtPlugin("community.flock" % "sbt-bazel" % "${BuildInfo.version}")""".getBytes())

  test("should install sbt project") {
    val path = fileFromPath("/simple-zio-project")
    writePluginFile(path)
    assertEquals(run(path, "sbt bazelInstall"), 0)
    assertEquals(run(path, "bazelisk build //modules/api"), 0)
  }

}
