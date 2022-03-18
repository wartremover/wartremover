package org.wartremover
package warts

object Null extends WartTraverser {
  private[this] val existsScalaXML: Boolean = {
    try {
      Class.forName("scala.xml.Elem")
      true
    } catch {
      case _: ClassNotFoundException =>
        false
    }
  }

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            val e = t.asExpr
            e match {
              case '{ null == ($x: Any) } =>
              case '{ null != ($x: Any) } =>
              case '{ ($x: Any) == null } =>
              case '{ ($x: Any) != null } =>
              case '{ null eq ($x: AnyRef) } =>
              case '{ null ne ($x: AnyRef) } =>
              case '{ ($x: AnyRef) eq null } =>
              case '{ ($x: AnyRef) ne null } =>
              case '{ ($x: Option[t]).orNull } =>
                error(tree.pos, "Option#orNull is disabled")
              case _ =>
                val continue = if (existsScalaXML) {
                  e match {
                    case '{ new scala.xml.Elem(null, $x1, $x2, $x3, $x4) } =>
                      false
                    case '{ new scala.xml.NamespaceBinding(null, $x1, $x2) } =>
                      false
                    case _ =>
                      true
                  }
                } else {
                  true
                }

                if (continue) {
                  e match {
                    case '{ null } =>
                      error(tree.pos, "null is disabled")
                    case _ =>
                      super.traverseTree(tree)(owner)
                  }
                }
            }
          case t @ ValDef(_, _, Some(Wildcard()))
              if t.symbol.flags.is(Flags.Mutable) && t.tpt.tpe <:< TypeRepr.of[AnyRef] =>
            error(tree.pos, "null is disabled")

          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
