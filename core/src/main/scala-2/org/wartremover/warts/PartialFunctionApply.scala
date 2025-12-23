package org.wartremover
package warts

object PartialFunctionApply extends WartTraverser with PartialFunctionApplyCompat {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val pfType = rootMirror.staticClass("scala.PartialFunction").toTypeConstructor
    val seqType = rootMirror.staticClass(seqTypeName).toTypeConstructor
    val mapType = rootMirror.staticClass(mapTypeName).toTypeConstructor

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) || isSynthetic(u)(t) =>
          case If(
                Apply(Select(Ident(x1), TermName("isDefinedAt")), Ident(x2) :: Nil),
                Apply(Select(Ident(x3), TermName("apply")), Ident(x4) :: Nil),
                elseTerm
              ) if (x1 == x3) && (x2 == x4) =>
            super.traverse(elseTerm)
          case CaseDef(
                _,
                Apply(Select(Ident(x1), TermName("isDefinedAt")), Ident(x2) :: Nil),
                Apply(Select(Ident(x3), TermName("apply")), Ident(x4) :: Nil)
              ) if (x1 == x3) && (x2 == x4) =>
          case Apply(Select(obj, TermName("apply")), _ :: Nil)
              if (obj.tpe.dealias.typeConstructor <:< pfType) && !(obj.tpe.dealias.typeConstructor <:< seqType) && !(obj.tpe.dealias.typeConstructor <:< mapType) =>
            error(u)(tree.pos, "PartialFunction#apply is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
