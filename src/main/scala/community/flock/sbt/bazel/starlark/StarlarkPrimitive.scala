package community.flock.sbt.bazel.starlark

sealed trait StarlarkPrimitive { self =>
  def expr: StarlarkExpr = StarlarkExpr.Primitive(self)
}

object StarlarkPrimitive {

  final case class Str(value: String) extends StarlarkPrimitive
  final case class Bool(value: Boolean) extends StarlarkPrimitive
  final case class Collection(values: List[StarlarkPrimitive]) extends StarlarkPrimitive

  def show(prim: StarlarkPrimitive): String = prim match {
    case Str(value) => s""""$value""""
    case Bool(value) => if (value) "True" else "False"
    case Collection(values) => s"[${values.map(StarlarkPrimitive.show).mkString(", ")}]"
  }
}