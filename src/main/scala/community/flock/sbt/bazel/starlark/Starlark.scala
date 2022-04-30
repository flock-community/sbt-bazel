package community.flock.sbt.bazel.starlark


object Starlark {
  def string(value: String) =
    StarlarkPrimitive.Str(value)

  def bool(value: Boolean)=
    StarlarkPrimitive.Bool(value)

  def list(values: List[StarlarkPrimitive]) =
    StarlarkPrimitive.Collection(values)

  def functionNamed(name: String, args: Map[String, StarlarkExpr]) =
    StarlarkExpr.Function(name, args.map { case (key, value) => Argument.Named(key, value) }.toList)

  def function(name: String, args: Argument*): StarlarkExpr.Function =
    StarlarkExpr.Function(name, args.toList)

}
