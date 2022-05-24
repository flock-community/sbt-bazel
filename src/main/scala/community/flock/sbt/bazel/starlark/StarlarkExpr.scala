package community.flock.sbt.bazel.starlark

sealed trait StarlarkExpr { self =>
  def stmt: StarlarkStmt = StarlarkStmt.Expr(self)
}

object StarlarkExpr {
  final case class Primitive(primitive: StarlarkPrimitive) extends StarlarkExpr
  final case class Function(name: String, arguments: List[Argument]) extends StarlarkExpr

  def show(expr: StarlarkExpr): String = expr match {
    case Primitive(primitive) => StarlarkPrimitive.show(primitive)
    case Function(name, arguments) => s"$name(${arguments.map(Argument.show).mkString(", ")})"
  }
}
