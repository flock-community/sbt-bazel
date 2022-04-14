package community.flock.sbt.starlark

import higherkindness.droste.data.Fix

object Starlark {
  def string(value: String): Starlark = Fix[StarlarkAst](StarlarkAst.Primitive(StarlarkPrimitive.Str(value)))
  def bool(value: Boolean): Starlark = Fix[StarlarkAst](StarlarkAst.Primitive(StarlarkPrimitive.Bool(value)))
  def list(values: List[Fix[StarlarkAst]]): Starlark = Fix[StarlarkAst](StarlarkAst.Primitive(StarlarkPrimitive.Collection(values)))
  def function(name: String, args: Map[String, Fix[StarlarkAst]]): Starlark = Fix[StarlarkAst](StarlarkAst.Function(name, args))
  def build(calls: List[Fix[StarlarkAst]]): Starlark = Fix[StarlarkAst](StarlarkAst.Build(calls))
}
