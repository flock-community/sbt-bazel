package community.flock.sbt

import java.io.File
import scala.sys.process.*

class IntegrationSpec extends munit.FunSuite {

  def run(cmd: String) =
    Process(cmd, new File("/tmp/zio-test")).run().exitValue()

  test("should install sbt project") {
    Projects.zio.write
    assertEquals(run("sbt bazelSetup bazelModules bazelDeps"), 0)
    assertEquals(run("bazelisk build //modules/api"), 0)
  }

}
