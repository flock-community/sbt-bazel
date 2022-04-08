package community.flock.sbt.starlak

import higherkindness.droste.data.Fix

object Starlak {
  def string(value: String): Starlak = Fix[StarlakAst](StarlakAst.Primitive(StarlakPrimitive.Str(value)))
  def bool(value: Boolean): Starlak = Fix[StarlakAst](StarlakAst.Primitive(StarlakPrimitive.Bool(value)))
  def list(values: List[Fix[StarlakAst]]): Starlak = Fix[StarlakAst](StarlakAst.Primitive(StarlakPrimitive.Collection(values)))
  def function(name: String, args: Map[String, Fix[StarlakAst]]): Starlak = Fix[StarlakAst](StarlakAst.Function(name, args))
  def build(calls: List[Fix[StarlakAst]]): Starlak = Fix[StarlakAst](StarlakAst.Build(calls))
}
