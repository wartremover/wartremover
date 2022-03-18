package org.wartremover
package warts

object StringPlusAny extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Plus = TermName("$plus")
    val PredefName = TermName("Predef")
    val Any2StringAddName = TermName("any2stringadd")
    val StringCanBuildFromName = TermName("StringCanBuildFrom")

    def isString(t: Tree): Boolean = t.tpe <:< typeOf[String]

    def isStringExpression(t: Tree): Boolean = t match {
      //workaround: type of some expressions is inferred to Any during wart search since Scala 2.11
      //this check doesn't cover all possible cases
      case Block(_, expr) => isStringExpression(expr)
      case If(_, thn, els) => isStringExpression(thn) && isStringExpression(els)
      case Apply(_, List(Select(Select(_, PredefName), StringCanBuildFromName))) => true
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

          case Apply(Select(lhs, Plus), List(rhs))
              if isPrimitive(u)(lhs.tpe) && isStringExpression(rhs) =>
            error(u)(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case t @ Apply(Select(lhs, Plus), List(rhs))
              if isString(lhs) && !isStringExpression(rhs) && !isSynthetic(u)(t) =>
            error(u)(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case _ => super.traverse(tree)
        }
      }
    }
  }
}
