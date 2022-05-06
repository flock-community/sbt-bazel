package community.flock.sbt.bazel.starlark

import cats.implicits._
import cats.{Applicative, Traverse}
import higherkindness.droste._
import higherkindness.droste.util.DefaultTraverse

sealed trait StarlarkAst[+A]

object StarlarkAst {

  //TODO: this contains invalid recursive paths
  final case class Primitive[A](value: StarlarkPrimitive[A]) extends StarlarkAst[A]
  final case class Function[A](name: String, arguments: Map[String, A]) extends StarlarkAst[A]
  final case class Build[A](calls: List[A]) extends StarlarkAst[A]

  implicit val traverseInstance: Traverse[StarlarkAst] = new DefaultTraverse[StarlarkAst] {
    override def traverse[G[_], A, B](fa: StarlarkAst[A])(f: A => G[B])(implicit A: Applicative[G]): G[StarlarkAst[B]] = fa match {
      case Primitive(value) => Traverse[StarlarkPrimitive].traverse(value)(f).map(Primitive.apply)
      case Function(name, arguments) => Traverse[Map[String, *]].traverse(arguments)(f).map(Function(name, _))
      case Build(calls) => Traverse[List].traverse(calls)(f).map(Build.apply)
    }
  }

  val render: Algebra[StarlarkAst, String] = Algebra {
    case Primitive(value) =>
      StarlarkPrimitive.render(value)
    case Function(name, arguments) =>
      s"$name(${arguments.map { case (key, value) => s"$key = $value"}.mkString(", ")})"
    case Build(calls) =>
      calls.mkString("\n")
  }
}


