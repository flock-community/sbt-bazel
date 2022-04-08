package flock

import sbt._

object BazelPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val bazelModules =
      taskKey[Unit]("Bazel-ify modules, introspects project settings and exports to BUILD per module")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    bazelModules := ()
  )
}
