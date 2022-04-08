package community.flock.sbt.starlak

import cats._
import cats.implicits._
import higherkindness.droste._
import higherkindness.droste.util.DefaultTraverse

sealed trait StarlakPrimitive[+A]

object StarlakPrimitive {

  final case class Str(value: String) extends StarlakPrimitive[Nothing]

  final case class Bool(value: Boolean) extends StarlakPrimitive[Nothing]

  final case class Collection[A](values: List[A]) extends StarlakPrimitive[A]

  implicit val traverseInstance: Traverse[StarlakPrimitive] = new DefaultTraverse[StarlakPrimitive] {
    override def traverse[G[_], A, B](fa: StarlakPrimitive[A])(f: A => G[B])(implicit A: Applicative[G]): G[StarlakPrimitive[B]] = fa match {
      case Str(value) => A.pure(Str(value))
      case Bool(value) => A.pure(Bool(value))
      case Collection(values) => Traverse[List].traverse(values)(f).map(Collection.apply)
    }
  }

  val render: Algebra[StarlakPrimitive, String] = Algebra {
    case Str(value) => s""""$value""""
    case Bool(value) => if (value) "True" else "False"
    case Collection(values) => s"[${values.mkString(", ")}]"
  }
}