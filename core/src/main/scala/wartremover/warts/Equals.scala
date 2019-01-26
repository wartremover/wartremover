package org.wartremover
package warts

import reflect.NameTransformer

object Equals extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Eqeq = TermName(NameTransformer.encode("=="))
    val NotEq = TermName(NameTransformer.encode("!="))
    val Equals = TermName("equals")
    val Eq = TermName("eq")
    val Ne = TermName("ne")
    val IsDefinedAt = TermName("isDefinedAt")

    new Traverser {
      override def traverse(tree: Tree) = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case Function(_, body) => traverse(body)

          case ClassDef(_, _, _, Template((_, _, statements))) if isSyntheticPartialFunction(u)(tree) =>
            statements.foreach {
              case DefDef(_, IsDefinedAt, _, _, _, _) =>
              case t =>
                traverse(t)
            }

          case _ if isSynthetic(u)(tree) =>

          case Apply(Select(lhs, Eqeq), _) =>
            error(u)(tree.pos, "== is disabled - use === or equivalent instead")

          case Apply(Select(lhs, NotEq), _) =>
            error(u)(tree.pos, "!= is disabled - use =/= or equivalent instead")

          case Apply(Select(lhs, Equals), _)
            if (tree.symbol :: tree.symbol.overrides).exists(_.fullName == "scala.Any.equals") =>
            error(u)(tree.pos, "equals is disabled - use === or equivalent instead")

          case Apply(Select(lhs, Eq), _) =>
            error(u)(tree.pos, "eq is disabled - use === or equivalent instead")

          case Apply(Select(lhs, Ne), _) =>
            error(u)(tree.pos, "ne is disabled - use =/= or equivalent instead")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}
