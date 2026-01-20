package org.wartremover
package warts

object Null extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      private val constructorDefaultNamePrefix = scala.reflect.NameTransformer.encode("<init>") + "$default"
      import q.reflect.*

      private object OptionOrNull {
        def unapply(t: Tree): Boolean = t match {
          case _ if sourceCodeNotContains(t, "orNull") =>
            false
          case _ if t.isExpr =>
            t.asExpr match {
              case '{ ($x: Option[?]).orNull } =>
                true
              case _ =>
                false
            }
          case _ =>
            false
        }
      }

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
          case Apply(Select(Literal(NullConstant()), "==" | "!=" | "eq" | "ne"), other) =>
            other.foreach(traverseTree(_)(owner))
          case Apply(Select(other, "==" | "!=" | "eq" | "ne"), Literal(NullConstant()) :: Nil) =>
            traverseTree(other)(owner)
          case OptionOrNull() =>
            error(tree.pos, "Option#orNull is disabled")
          case Apply(Select(left, _), _) if left.tpe.typeSymbol.fullName.startsWith("scala.xml.") =>
          case Literal(NullConstant()) =>
            error(tree.pos, "null is disabled")
          case t @ ValDef(_, _, Some(Wildcard()))
              if t.symbol.flags.is(Flags.Mutable) && t.tpt.tpe <:< TypeRepr.of[AnyRef] =>
            error(tree.pos, "null is disabled")
          case t @ ValDef(_, _, Some(rhs))
              if t.symbol.flags.is(Flags.Mutable) && t.tpt.tpe <:< TypeRepr
                .of[AnyRef] && rhs.symbol.fullName == "scala.compiletime.package$package$.uninitialized" =>
            error(tree.pos, "null is disabled")

          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
