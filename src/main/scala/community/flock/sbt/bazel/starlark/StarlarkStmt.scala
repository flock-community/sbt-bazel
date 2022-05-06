package community.flock.sbt.bazel.starlark

sealed trait Argument

object Argument {

  final case class Named(name: String, value: StarlarkExpr) extends Argument
  final case class Literal(value: String) extends Argument

  def show(stmt: Argument): String = stmt match {
    case Named(name, value) => s"$name = ${StarlarkExpr.show(value)}"
    case Literal(value) => s""""$value""""
  }
}

sealed trait StarlarkStmt

object StarlarkStmt {

  final case class Assign(name: String, expr: StarlarkExpr) extends StarlarkStmt
  final case class Load(file: String, stmts: List[Argument]) extends StarlarkStmt
  final case class Expr(expr: StarlarkExpr) extends StarlarkStmt

  def show(stmt: StarlarkStmt) = stmt match {
    case Assign(name, expr) => s"$name = ${StarlarkExpr.show(expr)}"
    case Load(file, stmts) => s"""load("$file", ${stmts.map(Argument.show).mkString(", ")})"""
    case Expr(expr) => StarlarkExpr.show(expr)
  }
}
