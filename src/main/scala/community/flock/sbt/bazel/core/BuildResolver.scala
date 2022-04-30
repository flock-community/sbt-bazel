package community.flock.sbt.bazel.core

sealed trait BuildResolver extends Product with Serializable {
  def show: String
}

object BuildResolver {
  final case class Credentials(user: String, pass: String) {
    def show = s"$user:$pass"
  }

  final case class Maven(name: String, location: String, credentials: Option[Credentials]) extends BuildResolver {
    def show: String = location
  }

  final case class Ivy(name: String, pattern: String, credentials: Option[Credentials]) extends BuildResolver {
    def show: String = pattern
  }
}