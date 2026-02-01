package org.wartremover
package warts

import reflect.NameTransformer

object ArrayEquals extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Equals = "equals"
    val EqualsSymbol = NameTransformer.encode("==")
    val NotEqualsSymbol = NameTransformer.encode("!=")

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Apply(Select(array, TermName(EqualsSymbol | NotEqualsSymbol)), Seq(Literal(Constant(null)))) =>
            // https://github.com/wartremover/wartremover/issues/448
            super.traverse(tree)
          case Apply(Select(array, TermName(op)), _)
              if (array.tpe.typeSymbol.fullName == "scala.Array" || array.tpe <:< typeOf[
                Iterator[Any]
              ]) && !isSynthetic(u)(tree) =>
            op match {
              case Equals =>
                error(u)(tree.pos, "equals is disabled")
              case EqualsSymbol =>
                error(u)(tree.pos, "== is disabled")
              case NotEqualsSymbol =>
                error(u)(tree.pos, "!= is disabled")
              case _ =>
                super.traverse(tree)
            }
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
