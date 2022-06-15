package community.flock.sbt.bazel.starlark

final case class StarlarkProgram(statements: List[StarlarkStmt])

object StarlarkProgram {
  def of(stmts: List[StarlarkStmt]): StarlarkProgram = StarlarkProgram(stmts)

  def of(stmts: StarlarkStmt*): StarlarkProgram = StarlarkProgram(stmts.toList)

  def show(prg: StarlarkProgram): String = prg.statements.map(StarlarkStmt.show).mkString("\n")
}
