package org.wartremover
package warts

object StringPlusAny extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Plus: TermName = "$plus"

    def isString(t: Tree) = t.tpe <:< typeOf[String]

    def isStringExpression(t: Tree) = t match {
      //workaround: type of some expressions is inferred to Any during wart search since Scala 2.11
      //this check doesn't cover all possible cases
      case If(_, thn, els) => isString(thn) && isString(els)
      case _ => isString(t)
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case t @ Apply(Select(lhs, Plus), List(rhs)) if isString(t) && (isString(lhs) ^ isStringExpression(rhs))
              && !isSynthetic(u)(t) =>
            u.error(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case _ => super.traverse(tree)
        }
      }
    }
  }
}
