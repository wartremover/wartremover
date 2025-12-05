package org.wartremover
package warts

object PartialFunctionApply extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val pfType = rootMirror.staticClass("scala.PartialFunction").toTypeConstructor
    val seqType = rootMirror.staticClass("scala.collection.Seq").toTypeConstructor
    val mapType = rootMirror.staticClass("scala.collection.Map").toTypeConstructor

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) || isSynthetic(u)(t) =>
          case Apply(Select(obj, TermName("apply")), _ :: Nil)
              if (obj.tpe.typeConstructor <:< pfType) && !(obj.tpe.typeConstructor <:< seqType) && !(obj.tpe.typeConstructor <:< mapType) =>
            error(u)(tree.pos, "PartialFunction#apply is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
