package org.wartremover
package warts

object StringPlusAny extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Plus: TermName = "$plus"
    val PredefName: TermName = "Predef"
    val Any2StringAddName: TermName = "any2stringadd"

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case Apply(Select(Apply(Select(Select(_, PredefName), Any2StringAddName), _), Plus), _) =>
            u.error(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)
          case Apply(Select(TypeApply(Select(Select(_, PredefName), Any2StringAddName), _), Plus), _) =>
            u.error(tree.pos, "Implicit conversion to string is disabled")
            super.traverse(tree)

          case Apply(Select(lhs, Plus), List(rhs))
            if (lhs.tpe <:< typeOf[String] ^ rhs.tpe <:< typeOf[String]) && !isSynthetic(u)(tree) =>
            val validIf = rhs match {
              case If(_, then, els) => then.tpe <:< typeOf[String] && els.tpe <:< typeOf[String]
              case _ => false
            }
            val overriddenPlus = lhs.symbol != null && lhs.symbol.owner != null && {
              val owner = lhs.symbol.owner
              owner.isClass && !owner.isSynthetic && owner.typeSignature.members.exists { x =>
                x.isMethod && x.name.toTermName == Plus && !x.annotations.exists(isWartAnnotation(u))
              }
            }
            if (!(overriddenPlus || validIf)) {
              u.error(tree.pos, "Implicit conversion to string is disabled")
            }
            super.traverse(tree)

          case _ => super.traverse(tree)
        }
      }
    }
  }
}
