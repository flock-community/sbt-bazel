package community.flock.sbt.renderer

import community.flock.sbt.core.{BuildArtifactId, BuildDependency}

class BazelDepsRendererSpec  extends munit.FunSuite {

  val fs2 = BuildDependency("co.fs2", BuildArtifactId("fs2-core", Some("fs2-core")), "3.2.7", Some("1.6.2"), Some("2.13"))
  val itext = BuildDependency("itext", BuildArtifactId("itext", None), "1.4.1", Some("1.6.2"), None)

  test("scala dependency") {
    assertEquals(BazelDepsRenderer.render(fs2), "@jvm_deps//:co_fs2_fs2_core_2_13")
  }
  test("java dependency") {
    assertEquals(BazelDepsRenderer.render(itext), "@jvm_deps//:itext_itext")
  }
}
