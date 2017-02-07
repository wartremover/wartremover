package org.wartremover
package warts

object StringPlusAny extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Plus: TermName = "$plus"
    val PredefName: TermName = "Predef"
    val Any2StringAddName: TermName = "any2stringadd"

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

          case Apply(Select(Select(_, PredefName), Any2StringAddName), _) =>
            error(u)(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)
          case TypeApply(Select(Select(_, PredefName), Any2StringAddName), _) =>
            error(u)(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case Apply(Select(Literal(Constant(c)), Plus), _) if !c.isInstanceOf[String] =>
            error(u)(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case t @ Apply(Select(lhs, Plus), List(rhs)) if isString(lhs) && !isStringExpression(rhs)
              && !isSynthetic(u)(t) =>
            error(u)(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case _ => super.traverse(tree)
        }
      }
    }
  }
}
