package community.flock.sbt.starlak

import cats.implicits._
import cats.{Applicative, Traverse}
import higherkindness.droste._
import higherkindness.droste.util.DefaultTraverse

sealed trait StarlakAst[+A]

object StarlakAst {
  final case class Primitive[A](value: StarlakPrimitive[A]) extends StarlakAst[A]
  final case class Function[A](name: String, arguments: Map[String, A]) extends StarlakAst[A]
  final case class Build[A](calls: List[A]) extends StarlakAst[A]

  implicit val traverseInstance: Traverse[StarlakAst] = new DefaultTraverse[StarlakAst] {
    override def traverse[G[_], A, B](fa: StarlakAst[A])(f: A => G[B])(implicit A: Applicative[G]): G[StarlakAst[B]] = fa match {
      case Primitive(value) => Traverse[StarlakPrimitive].traverse(value)(f).map(Primitive.apply)
      case Function(name, arguments) => Traverse[Map[String, *]].traverse(arguments)(f).map(Function(name, _))
      case Build(calls) => Traverse[List].traverse(calls)(f).map(Build.apply)
    }
  }

  val render: Algebra[StarlakAst, String] = Algebra {
    case Primitive(value) =>
      StarlakPrimitive.render(value)
    case Function(name, arguments) =>
      s"$name(${arguments.map { case (key, value) => s"$key = $value"}.mkString(", ")})"
    case Build(calls) =>
      calls.mkString("\n")
  }
}


