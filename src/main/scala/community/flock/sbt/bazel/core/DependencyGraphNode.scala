package community.flock.sbt.bazel.core

import sbt.{ProjectRef, ResolvedProject}


sealed trait DependencyKind

object DependencyKind {
  case object DependsOn extends DependencyKind
  case object Aggregate extends DependencyKind
}

final case class Dependency[A](target: A, kind: DependencyKind) {
  def map[B](f: A => B): Dependency[B] = Dependency(f(target), kind)
}

object Dependency {
  def fromAggregate[A](target: A) = Dependency(target, DependencyKind.Aggregate)
  def fromDependsOn[A](target: A) = Dependency(target, DependencyKind.DependsOn)
}

sealed trait EdgeKind

object EdgeKind {
  case object Solid extends EdgeKind
  case object Dashed extends EdgeKind
}

final case class Edge[A](from: A, to: A, kind: EdgeKind)

object Edge {
  def fromDependency[A](from: A, to: Dependency[A]): Edge[A] = {
    val kind = to.kind match {
      case DependencyKind.DependsOn => EdgeKind.Solid
      case DependencyKind.Aggregate => EdgeKind.Dashed
    }
    Edge(from = from, to = to.target, kind = kind)
  }
}

final case class DependencyGraphNode[A](value: A, directDeps: Set[Dependency[DependencyGraphNode[A]]], allDeps: Set[Dependency[A]], allEdges: Set[Edge[A]])

object DependencyGraphNode {
  def fromResolvedProject(p: ResolvedProject, projects: Map[String, ResolvedProject]): DependencyGraphNode[ResolvedProject] = {
    val aggregates = p.aggregate.toSet[ProjectRef].flatMap(ref => projects.get(ref.project).map(Dependency.fromAggregate))
    val classpathDeps = p.dependencies.flatMap(dep => projects.get(dep.project.project).map(Dependency.fromDependsOn))

    val directDeps0:    Set[Dependency[ResolvedProject]]       =  aggregates ++ classpathDeps
    val directDeps:     Set[Dependency[DependencyGraphNode[ResolvedProject]]] = directDeps0 map (d => Dependency(fromResolvedProject(d.target, projects), d.kind))
    val transDeps:      Set[Dependency[ResolvedProject]]       = directDeps flatMap (_.target.allDeps)
    val uniqDirectDeps: Set[Dependency[DependencyGraphNode[ResolvedProject]]] = directDeps filterNot (d => transDeps(d.map(_.value)))


    DependencyGraphNode(
      value = p,
      directDeps = uniqDirectDeps,
      allDeps = directDeps0 ++ transDeps,
      allEdges = uniqDirectDeps.flatMap(_.target.allEdges) ++ uniqDirectDeps.map(d => Edge.fromDependency(p, d.map(_.value)))
    )
  }
}