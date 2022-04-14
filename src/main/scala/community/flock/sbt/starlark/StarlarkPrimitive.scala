package community.flock.sbt.starlark

import cats._
import cats.implicits._
import higherkindness.droste._
import higherkindness.droste.util.DefaultTraverse

sealed trait StarlarkPrimitive[+A]

object StarlarkPrimitive {

  final case class Str(value: String) extends StarlarkPrimitive[Nothing]

  final case class Bool(value: Boolean) extends StarlarkPrimitive[Nothing]

  final case class Collection[A](values: List[A]) extends StarlarkPrimitive[A]

  implicit val traverseInstance: Traverse[StarlarkPrimitive] = new DefaultTraverse[StarlarkPrimitive] {
    override def traverse[G[_], A, B](fa: StarlarkPrimitive[A])(f: A => G[B])(implicit A: Applicative[G]): G[StarlarkPrimitive[B]] = fa match {
      case Str(value) => A.pure(Str(value))
      case Bool(value) => A.pure(Bool(value))
      case Collection(values) => Traverse[List].traverse(values)(f).map(Collection.apply)
    }
  }

  val render: Algebra[StarlarkPrimitive, String] = Algebra {
    case Str(value) => s""""$value""""
    case Bool(value) => if (value) "True" else "False"
    case Collection(values) => s"[${values.mkString(", ")}]"
  }
}