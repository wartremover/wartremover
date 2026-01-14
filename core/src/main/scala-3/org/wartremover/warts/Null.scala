package org.wartremover
package warts

object Null extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      private val constructorDefaultNamePrefix = scala.reflect.NameTransformer.encode("<init>") + "$default"
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: ClassDef if t.symbol.flags.is(Flags.Module) =>
            traverseTree(t.constructor)(owner)
            t.parents.foreach(traverseTree(_)(owner))
            t.self.foreach(traverseTree(_)(owner))
            t.body.filter {
              case dd: DefDef
                  if dd.name
                    .startsWith(constructorDefaultNamePrefix) && hasWartAnnotationSymbol(t.symbol.companionClass) =>
                false
              case _ =>
                true
            }.foreach(traverseTree(_)(owner))
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
              case '{ ($x: Option[?]).orNull } =>
                error(tree.pos, "Option#orNull is disabled")
              case _ =>
                tree.match {
                  case Apply(Select(left, _), _) =>
                    !left.tpe.typeSymbol.fullName.startsWith("scala.xml.")
                  case _ =>
                    true
                }.match {
                  case true =>
                    e match {
                      case '{ null } =>
                        error(tree.pos, "null is disabled")
                      case _ =>
                        super.traverseTree(tree)(owner)
                    }
                  case _ =>
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
