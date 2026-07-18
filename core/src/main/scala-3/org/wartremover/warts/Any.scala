package org.wartremover
package warts

import dotty.tools.dotc.ast.tpd.InferredTypeTree

object Any extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case Apply(Select(Apply(Select(s, "apply"), args), "s"), _) if s.symbol.fullName == "scala.StringContext" =>
            // https://github.com/wartremover/wartremover/issues/1799
            args.foreach(a => super.traverseTree(a)(owner))
          case a: Inferred
              if a.tpe =:= TypeRepr.of[scala.Any] && a
                .isInstanceOf[InferredTypeTree] && !a.symbol.flags.is(Flags.JavaDefined) =>
            error(tree.pos, "Inferred type containing Any: Any")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
