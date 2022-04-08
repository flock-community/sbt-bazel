package community.flock.sbt

import cats.{Applicative, Traverse}
import cats.implicits.*
import higherkindness.droste.data.Fix
import higherkindness.droste.util.DefaultTraverse

package object starlak {
  implicit def traverseMap[K]: Traverse[Map[K, *]] = new DefaultTraverse[Map[K, *]] {
    override def traverse[G[`2`], A, B](fa: Map[K, A])(f: A => G[B])(implicit A: Applicative[G]): G[Map[K, B]] =
      fa.toList.traverse { case (key, value) => f(value).map(key -> _)}.map(_.toMap)
  }

  type Starlak = Fix[StarlakAst]
}
